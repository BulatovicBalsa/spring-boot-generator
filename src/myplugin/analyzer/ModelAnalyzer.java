package myplugin.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import lombok.Getter;
import lombok.Setter;
import myplugin.generator.fmmodel.*;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;

import static myplugin.generator.StringUtil.*;

/**
 * Analyzer for JPA entity generation with basic relations:
 * - MANY_TO_ONE and ONE_TO_MANY(mappedBy=...)

 * Relations are inferred from attributes whose type is another UML Class in the model.
 * Multiplicity upper is used to detect collections.
 */
@Getter
@Setter
public class ModelAnalyzer {

	private final Package root;

	private final Map<String, FMClass> fmClassByName = new HashMap<>();

	public ModelAnalyzer(Package root) {
		this.root = root;
	}

	public void prepareModel() throws AnalyzeException {
		FMModel.getInstance().getClasses().clear();
		if (FMModel.getInstance().getEnumerations() != null) {
			FMModel.getInstance().getEnumerations().clear();
		}
		fmClassByName.clear();

		processPackage(root);

		for (FMClass c : FMModel.getInstance().getClasses()) {
			fmClassByName.put(c.getName(), c);
		}

		// 2) determine relations MANY_TO_ONE / ONE_TO_MANY(mappedBy)
		inferRelations();
	}

	private void processPackage(Package pack) throws AnalyzeException {
		if (pack.getName() == null) {
			throw new AnalyzeException("Packages must have names!");
		}
		if (!pack.hasOwnedElement()) return;
		// Extract enumerations
		for (Element ownedElement : pack.getOwnedElement()) {
			if (ownedElement instanceof Enumeration) {
				Enumeration en = (Enumeration) ownedElement;
				FMEnumeration fmEnum = new FMEnumeration(en.getName(), "");
				for (EnumerationLiteral lit : en.getOwnedLiteral()) {
					fmEnum.getValues().add(lit.getName());
				}
				FMModel.getInstance().getEnumerations().add(fmEnum);
			}
		}
		// Extract classes
		for (Element ownedElement : pack.getOwnedElement()) {
			if (ownedElement instanceof Stereotype) continue;
			if (ownedElement instanceof Class) {
				Class cl = (Class) ownedElement;
				FMClass fmClass = getClassData(cl);
				FMModel.getInstance().getClasses().add(fmClass);
			}
		}
	}

	private void addPropertiesFromClassMembers(Class cl, FMClass fmClass) throws AnalyzeException {
		Set<String> seen = new HashSet<>();
		for (Property p : cl.getOwnedAttribute()) {
			String key = propertyKey(p);
			if (!seen.contains(key)) {
				seen.add(key);
				fmClass.getProperties().add(getPropertyData(p, cl));
			}
		}
	}

	private FMClass getClassData(Class cl) throws AnalyzeException {
		if (cl.getName() == null) {
			throw new AnalyzeException("Classes must have names!");
		}

		FMClass fmClass = new FMClass(cl.getName());

		Stereotype embeddableStereo = StereotypesHelper.getAppliedStereotypeByString(cl, "Embeddable");
		if (embeddableStereo != null) {
			fmClass.setEmbeddable(true);
		}

		addPropertiesFromClassMembers(cl, fmClass);
		applyClassRepositoryQuery(cl, fmClass);
		return fmClass;
	}

	private FMProperty getPropertyData(Property p, Class cl) throws AnalyzeException {
		String n = (isNullOrEmpty(p.getName())) ? "<unnamed>" : p.getName();

		Type attType = p.getType();
		if (attType == null) {
			throw new AnalyzeException("Property " + cl.getName() + "." + n + " must have type!");
		}

		String typeName = attType.getName();
		if (typeName == null) {
			throw new AnalyzeException("Type of the property " + cl.getName() + "." + n + " must have name!");
		}

		boolean isEnum = FMModel.getInstance().hasEnumeration(typeName);

		// Multiplicity: upper == -1 => *, upper > 1 => collection
		int upper = p.getUpper();
		boolean isCollection = (upper == -1 || upper > 1);

		// 1) role name (attribute/association end name)
		String attName = p.getName();

		// 2) if role name is empty, try association name
		if (isNullOrEmpty(attName)) {
			Association assoc = p.getAssociation();
			if (assoc != null && !isNullOrEmpty(assoc.getName())) {
				attName = assoc.getName();
			}
		}

		// 3) last fallback: make name from type (Course -> course, Student[*] -> students)
		if (isNullOrEmpty(attName)) {
			attName = decapitalize(typeName);
			if (isCollection) {
				attName = pluralizeSimple(attName);
			}
		}

		if (isNullOrEmpty(attName)) {
			throw new AnalyzeException("Properties of the class: " + cl.getName() + " must have names!");
		}

		boolean isId = StereotypesHelper.getAppliedStereotypeByString(p, "Id") != null
				|| "id".equalsIgnoreCase(attName);

		FMProperty fp = new FMProperty(attName, typeName, isId);
		fp.setCollection(isCollection);
		fp.setEnumeration(isEnum);
		applyTaggedValueConstraints(p, fp);
		applyRepositoryQueryTaggedValues(p, fp);
		return fp;
	}

	private void applyTaggedValueConstraints(Property source, FMProperty target) {
		for (Stereotype stereotype : StereotypesHelper.getStereotypes(source)) {
			applyTaggedValuesFromStereotype(source, target, stereotype);
		}

		applyTaggedValuesFromStereotype(source, target, "Validation");
		applyTaggedValuesFromStereotype(source, target, "validation");
		applyTaggedValuesFromStereotype(source, target, "Constraints");
		applyTaggedValuesFromStereotype(source, target, "constraints");
		applyTaggedValuesFromStereotype(source, target, "Column");
		applyTaggedValuesFromStereotype(source, target, "column");
	}

	private void applyTaggedValuesFromStereotype(Property source, FMProperty target, Stereotype stereotype) {
		if (stereotype == null) return;

		for (Property tag : stereotype.getOwnedAttribute()) {
			List<?> values = StereotypesHelper.getStereotypePropertyValue(source, stereotype, tag.getName());
			if (values == null || values.isEmpty()) continue;
			Object raw = values.get(0);
			mapTagValueToConstraint(tag.getName(), raw, target);
		}
	}

	private void applyTaggedValuesFromStereotype(Property source, FMProperty target, String stereotypeName) {
		Stereotype stereotype = StereotypesHelper.getAppliedStereotypeByString(source, stereotypeName);
		if (stereotype == null) return;
		applyTaggedValuesFromStereotype(source, target, stereotype);
	}

	private void mapTagValueToConstraint(String rawTagName, Object rawValue, FMProperty target) {
		if (rawTagName == null) return;
		String tag = normalizeTagName(rawTagName);

		if ("nullable".equals(tag) || "null".equals(tag) || "optional".equals(tag)) {
			Boolean value = parseBoolean(rawValue);
			if (value != null) target.setNullable(value);
			return;
		}

		if ("notnull".equals(tag) || "required".equals(tag) || "mandatory".equals(tag)) {
			Boolean value = parseBoolean(rawValue);
			if (value != null) target.setNullable(!value);
			return;
		}

		if ("unique".equals(tag)) {
			Boolean value = parseBoolean(rawValue);
			if (value != null) target.setUnique(value);
			return;
		}

		if ("size".equals(tag) || "length".equals(tag) || "maxlength".equals(tag)) {
			Integer value = parseInteger(rawValue);
			if (value != null && value > 0) target.setSize(value);
			return;
		}

		if ("min".equals(tag) || "minvalue".equals(tag) || "minimum".equals(tag)) {
			String value = parseNumericText(rawValue);
			if (value != null) target.setMinValue(value);
			return;
		}

		if ("max".equals(tag) || "maxvalue".equals(tag) || "maximum".equals(tag)) {
			String value = parseNumericText(rawValue);
			if (value != null) target.setMaxValue(value);
			return;
		}

		if ("hide".equals(tag) || "internal".equals(tag) || tag.startsWith("hidden") || tag.startsWith("Hidden")) {
			Boolean value = parseBoolean(rawValue);
			if (value != null) target.setHidden(value);
			return;
		}
	}

	private String normalizeTagName(String name) {
		return name.toLowerCase().replace(" ", "").replace("_", "").replace("-", "");
	}

	private Boolean parseBoolean(Object value) {
		if (value instanceof Boolean) return (Boolean) value;
		if (value == null) return null;
		String text = String.valueOf(value).trim().toLowerCase();
		if ("true".equals(text) || "yes".equals(text) || "1".equals(text)) return Boolean.TRUE;
		if ("false".equals(text) || "no".equals(text) || "0".equals(text)) return Boolean.FALSE;
		return null;
	}

	private Integer parseInteger(Object value) {
		if (value instanceof Number) return ((Number) value).intValue();
		if (value == null) return null;
		try {
			return Integer.valueOf(String.valueOf(value).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String parseNumericText(Object value) {
		if (value == null) return null;
		String text = String.valueOf(value).trim();
		if (text.isEmpty()) return null;
		return text;
	}

	private String propertyKey(Property p) {
		String id = p.getID();
		if (id != null) return id;

		String n = p.getName();
		String t = (p.getType() != null) ? p.getType().getName() : "";
		String a = (p.getAssociation() != null && p.getAssociation().getName() != null) ? p.getAssociation().getName() : "";
		return (n == null ? "" : n) + "|" + t + "|" + a + "|" + System.identityHashCode(p);
	}

	/**
	 * Infer MANY_TO_ONE and ONE_TO_MANY based on:
	 * - Property type is another class in the model => relation
	 * - collection flag tells if it's plural
	 * - If A has collection of B AND B has single A => OneToMany/ManyToOne with mappedBy
	 * - Otherwise:
	 *   - single reference => MANY_TO_ONE (default)
	 *   - collection without backref => ONE_TO_MANY without mappedBy (unidirectional)
	 */
	private void inferRelations() {
		for (FMClass owner : FMModel.getInstance().getClasses()) {
			for (FMProperty p : owner.getProperties()) {

				FMClass target = fmClassByName.get(p.getType());
				if (target == null) {
					continue; // not a relation to another class in the model
				}

				if (target.isEmbeddable()) {
					p.setEmbedded(true);
					p.setRelation(false);
					p.setTargetClass(target.getName());
					continue;
				}

				p.setRelation(true);
				p.setTargetClass(target.getName());

				FMProperty opposite = findOpposite(target, owner.getName());

				boolean aMany = p.isCollection();
				boolean bExists = (opposite != null);
				boolean bMany = bExists && opposite.isCollection();

				// 1) MANY_TO_ONE / ONE_TO_MANY
				// A: many, B: one  => A is ONE_TO_MANY, B is MANY_TO_ONE
				if (aMany && bExists && !bMany) {
					p.setRelationKind(RelationKind.ONE_TO_MANY);
					p.setMappedBy(opposite.getName());

					markAsManyToOne(opposite, owner);
					continue;
				}

				// A: one, B: many  => A is MANY_TO_ONE, B is ONE_TO_MANY(mappedBy=A)
				if (!aMany && bExists && bMany) {
					p.setRelationKind(RelationKind.MANY_TO_ONE);
					// opposite side already has collection, so it will be ONE_TO_MANY(mappedBy=p.name)
					opposite.setRelation(true);
					opposite.setTargetClass(owner.getName());
					opposite.setRelationKind(RelationKind.ONE_TO_MANY);
					opposite.setMappedBy(p.getName());
					continue;
				}

				// 2) ONE_TO_ONE
				// A: one, B: one  => ONE_TO_ONE, ownership by class name
				if (!aMany && bExists) {
					p.setRelationKind(RelationKind.ONE_TO_ONE);

					// ownership: lexicographic order of class names (deterministic)
					boolean ownerIsOwningSide = isOwningSide(owner.getName(), target.getName());
					if (!ownerIsOwningSide) {
						// inverse side
						p.setMappedBy(opposite.getName());
					} else {
						p.setMappedBy(null); // owning side
					}

					// set opposite side symmetrically
					opposite.setRelation(true);
					opposite.setTargetClass(owner.getName());
					opposite.setRelationKind(RelationKind.ONE_TO_ONE);

					if (ownerIsOwningSide) {
						opposite.setMappedBy(p.getName()); // opposite is inverse
					} else {
						opposite.setMappedBy(null); // opposite is owning
					}
					continue;
				}

				// 3) MANY_TO_MANY
				// A: many, B: many => MANY_TO_MANY, ownership by class name
				if (aMany && bExists) {
					p.setRelationKind(RelationKind.MANY_TO_MANY);

					boolean ownerIsOwningSide = isOwningSide(owner.getName(), target.getName());
					if (!ownerIsOwningSide) {
						p.setMappedBy(opposite.getName()); // inverse
					} else {
						p.setMappedBy(null); // owning
					}

					// setuj opposite side symmetrically
					opposite.setRelation(true);
					opposite.setTargetClass(owner.getName());
					opposite.setRelationKind(RelationKind.MANY_TO_MANY);

					if (ownerIsOwningSide) {
						opposite.setMappedBy(p.getName()); // opposite inverse
					} else {
						opposite.setMappedBy(null); // opposite owning
					}
					continue;
				}

				// 4) Unidirectional cases (no opposite)
				if (!aMany) {
					// single reference without opposite -> default MANY_TO_ONE
					p.setRelationKind(RelationKind.MANY_TO_ONE);
					p.setMappedBy(null);
				} else {
					// collection without opposite -> default ONE_TO_MANY (unidirectional)
					p.setRelationKind(RelationKind.ONE_TO_MANY);
					p.setMappedBy(null);
				}
			}
		}
	}

	private void applyRepositoryQueryTaggedValues(Property source, FMProperty target) {
		Stereotype stereo = StereotypesHelper.getAppliedStereotypeByString(source, "RepositoryQuery");
		if (stereo == null) return;

		target.setSearchable(getBooleanValue(source, stereo, "searchable", true));
		target.setRangeQuery(getBooleanValue(source, stereo, "range", false));
	}

	private void applyClassRepositoryQuery(Class cl, FMClass fmClass) {
		Stereotype stereo = StereotypesHelper.getAppliedStereotypeByString(cl, "RepositoryQuery");
		if (stereo == null) return;

		fmClass.setPagination(getBooleanValue(cl, stereo, "pagination", false));
	}

	private Boolean getBooleanValue(Element element, Stereotype stereo, String tag, boolean defaultValue) {
		List<?> values = StereotypesHelper.getStereotypePropertyValue(element, stereo, tag);
		if (values == null || values.isEmpty()) return defaultValue;
		return (Boolean) values.get(0);
	}

	private void markAsManyToOne(FMProperty opposite, FMClass owner) {
		opposite.setRelation(true);
		opposite.setTargetClass(owner.getName());
		opposite.setRelationKind(RelationKind.MANY_TO_ONE);
		opposite.setMappedBy(null);
	}

	private boolean isOwningSide(String classA, String classB) {
		// owning side = class with lexicographically smaller name (deterministic)
		return classA.compareTo(classB) < 0;
	}

	private FMProperty findOpposite(FMClass targetClass, String ownerClassName) {
		for (FMProperty tp : targetClass.getProperties()) {
			if (ownerClassName.equals(tp.getType())) {
				return tp;
			}
		}
		return null;
	}

	private void classifyElementType(Class cl, FMClass fmClass) {
		Stereotype embeddableStereo = StereotypesHelper.getAppliedStereotypeByString(cl, "Embeddable");
		if (embeddableStereo != null) {
			fmClass.setEmbeddable(true);
			return;
		}
		fmClass.setEmbeddable(false);
	}
}
