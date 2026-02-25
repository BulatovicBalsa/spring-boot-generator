package myplugin.generator.options;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/** ProjectOptions: singleton class that guides code generation process
 * @ToDo: enable save to xml file and load from xml file for this class */

@Setter
@Getter
@NoArgsConstructor
public class ProjectOptions {
	//List of UML 2.0 to java (or any other destination language) mappings	
	private List<TypeMapping> typeMappings = new ArrayList<>();
	
	//Hash map for linking generators with its options
	private Map<String, GeneratorOptions> generatorOptions = new HashMap<>();
	
	private static ProjectOptions projectOptions = null;

	public static ProjectOptions getProjectOptions() {
		if (projectOptions ==null) { 
			projectOptions = new ProjectOptions();	
		}	
		return projectOptions;
	}

}
