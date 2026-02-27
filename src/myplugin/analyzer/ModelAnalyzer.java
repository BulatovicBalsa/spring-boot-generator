package myplugin.analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import lombok.Getter;
import lombok.Setter;
import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.fmmodel.FMProperty;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import myplugin.generator.fmmodel.RelationKind;

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

	private FMClass getClassData(Class cl) throws AnalyzeException {
		if (cl.getName() == null) {
			throw new AnalyzeException("Classes must have names!");
		}

		FMClass fmClass = new FMClass(cl.getName());

		Iterator<Property> it = ModelHelper.attributes(cl);
		while (it.hasNext()) {
			Property p = it.next();
			FMProperty prop = getPropertyData(p, cl);
			fmClass.getProperties().add(prop);
		}
		return fmClass;
	}

	private FMProperty getPropertyData(Property p, Class cl) throws AnalyzeException {
		String attName = p.getName();
		if (attName == null) {
			throw new AnalyzeException("Properties of the class: " + cl.getName() + " must have names!");
		}

		Type attType = p.getType();
		if (attType == null) {
			throw new AnalyzeException("Property " + cl.getName() + "." + attName + " must have type!");
		}

		String typeName = attType.getName();
		if (typeName == null) {
			throw new AnalyzeException("Type of the property " + cl.getName() + "." + attName + " must have name!");
		}

		boolean isId = StereotypesHelper.getAppliedStereotypeByString(p, "Id") != null
				|| "id".equalsIgnoreCase(attName);

		// Multiplicity: upper == -1 => *, upper > 1 => collection
		int upper = p.getUpper();
		boolean isCollection = (upper == -1 || upper > 1);

		FMProperty fp = new FMProperty(attName, typeName, isId);
		fp.setCollection(isCollection);
		return fp;
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
					continue;
				}

				// ovo je relacija
				p.setRelation(true);
				p.setTargetClass(target.getName());

				// nađi opposite property u target klasi (prvi koji pokazuje nazad na owner)
				FMProperty opposite = findOpposite(target, owner.getName());

				if (!p.isCollection()) {
					// single reference -> default MANY_TO_ONE
					p.setRelationKind(RelationKind.MANY_TO_ONE);
					// mappedBy ne ide na ManyToOne
					continue;
				}

				// collection
				if (opposite != null && !opposite.isCollection()) {
					// owner has many targets, target has one owner => ONE_TO_MANY(mappedBy=opposite.name)
					p.setRelationKind(RelationKind.ONE_TO_MANY);
					p.setMappedBy(opposite.getName());

					// na drugoj strani, ako još nije označeno kao relation, označi kao MANY_TO_ONE
					if (!opposite.isRelation()) {
						opposite.setRelation(true);
						opposite.setTargetClass(owner.getName());
					}
					opposite.setRelationKind(RelationKind.MANY_TO_ONE);
				} else {
					// unidirectional collection (bez backref-a)
					p.setRelationKind(RelationKind.ONE_TO_MANY);
					p.setMappedBy(null); // nema mappedBy
				}
			}
		}
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