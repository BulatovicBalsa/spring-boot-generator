package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** FMModel: Singleton class. This is intermediate data structure that keeps metadata
 * extracted from MagicDraw model. Data structure should be optimized for code generation
 * using freemarker
 */

@Setter
@Getter
public class FMModel {
	
	private List<FMClass> classes = new ArrayList<>();
	private List<FMEnumeration> enumerations = new ArrayList<>();
	
	//....
	/** @ToDo: Add lists of other elements, if needed */
	private FMModel() {
		
	}
	
	private static FMModel model;
	
	public static FMModel getInstance() {
		if (model == null) {
			model = new FMModel();			
		}
		return model;
	}

	public boolean hasEnumeration(String name) {
		for (FMEnumeration e : getInstance().getEnumerations()) {
			if (e.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public FMClass findEmbeddable(String className) {
		for (FMClass c : getClasses()) {
			if (className.equals(c.getName()) && c.isEmbeddable()) {
				return c;
			}
		}
		return null;
	}
}
