package myplugin.analyzer;

import java.util.Iterator;

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

/**
 * Minimal analyzer for JPA entity generation (no relations).
 * - Collects UML Classes and their attributes into FMModel
 * - Uses fixed java package (filePackage)
 * - Traverses only packages stereotyped as BusinessApp (kept from old logic)
 */
@Getter
@Setter
public class ModelAnalyzer {

	private final Package root;

	public ModelAnalyzer(Package root) {
		this.root = root;
	}

	public void prepareModel() throws AnalyzeException {
		FMModel.getInstance().getClasses().clear();
		// Enumerations not used in this phase
		if (FMModel.getInstance().getEnumerations() != null) {
			FMModel.getInstance().getEnumerations().clear();
		}

		// fixed package for generated code
		processPackage(root);
	}

	private void processPackage(Package pack) throws AnalyzeException {
		if (pack.getName() == null) {
			throw new AnalyzeException("Packages must have names!");
		}

		if (!pack.hasOwnedElement()) return;

		// 1) Extract classes from this package
		for (Element ownedElement : pack.getOwnedElement()) {
			if (ownedElement instanceof Stereotype) continue;
			if (ownedElement instanceof Class) {
				Class cl = (Class) ownedElement;
				FMClass fmClass = getClassData(cl); // <-- fixed package
				FMModel.getInstance().getClasses().add(fmClass);
			}
		}

		// 2) Recurse into child packages that are marked as BusinessApp
		for (Element ownedElement : pack.getOwnedElement()) {
			if (ownedElement instanceof Package) {
				Package ownedPackage = (Package) ownedElement;
				if (StereotypesHelper.getAppliedStereotypeByString(ownedPackage, "BusinessApp") != null) {
					processPackage(ownedPackage);
				}
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

        return new FMProperty(attName, typeName, isId);
	}
}