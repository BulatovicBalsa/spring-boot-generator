package myplugin.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import lombok.Getter;
import lombok.Setter;
import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.fmmodel.FMProperty;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import myplugin.generator.fmmodel.RelationKind;

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
			}		}
	}

	private FMClass getClassData(Class cl) throws AnalyzeException {
		if (cl.getName() == null) {
			throw new AnalyzeException("Classes must have names!");
		}

		FMClass fmClass = new FMClass(cl.getName());
		addPropertiesFromClassMembers(cl, fmClass);
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
		return fp;
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
}