package myplugin.generator.options;

import lombok.Getter;
import lombok.Setter;
import myplugin.generator.GenerationMode;
import myplugin.generator.IdStrategy;

/** GeneratorOptions: options used for code generation. Every generator (ejb generator, forms generator etc) should
 * have one instance of this class */

@Setter
@Getter
public class GeneratorOptions  {
	private String outputPath; 
	private String templateName;
	private String templateDir;
	private String outputFileName;
	private Boolean overwrite;
	private String filePackage;
	private IdStrategy idStrategy;
	private GenerationMode generationMode;
	
	public GeneratorOptions(String outputPath, String templateName,
			String templateDir, String outputFileName, Boolean overwrite,
			String filePackage, IdStrategy idStrategy) {
		this(outputPath, templateName, templateDir, outputFileName, overwrite,
				filePackage, idStrategy, GenerationMode.REST_ONLY);
	}

	public GeneratorOptions(String outputPath, String templateName,
			String templateDir, String outputFileName, Boolean overwrite,
			String filePackage, IdStrategy idStrategy, GenerationMode generationMode) {
		super();
		this.outputPath = outputPath;
		this.templateName = templateName;
		this.templateDir = templateDir;
		this.outputFileName = outputFileName;
		this.overwrite = overwrite;
		this.filePackage = filePackage;
		this.idStrategy = idStrategy;
		this.generationMode = generationMode;
	}
}
