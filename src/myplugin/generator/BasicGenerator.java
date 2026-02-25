package myplugin.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Objects;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import lombok.Getter;
import lombok.Setter;
import myplugin.generator.options.GeneratorOptions;

/**
 * Abstract generator that creates necessary environment for code generation 
 * (creating directory for code generation, fetching template, creating file with given name 
 * for code generation etc.). It should be ancestor for all generators in this project.
*/

@Setter
@Getter
public abstract class BasicGenerator {

    private String outputPath;
	private String templateName;
	private String templateDir;
	private String outputFileName;
	private boolean overwrite;
	private String filePackage;
	private Configuration cfg;
	private Template template;	
	
	public BasicGenerator(GeneratorOptions generatorOptions) {
        this.outputPath = generatorOptions.getOutputPath();
		this.templateName = generatorOptions.getTemplateName();
		this.templateDir = generatorOptions.getTemplateDir();
		this.outputFileName = generatorOptions.getOutputFileName();
		this.overwrite = generatorOptions.getOverwrite();
		this.filePackage = generatorOptions.getFilePackage();
	}

	public void generate() throws IOException {		
		if (outputPath == null) {
			throw new IOException("Output path is not defined!");
		}	
		if (templateName == null) {
			throw new IOException("Template name is not defined!");
		}
		if (outputFileName == null) {
			throw new IOException("Output file name is not defined!");
		}
		if (filePackage == null) {
			throw new IOException("Package name for code generation is not defined!");
		}

		cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);		

		final String tName = templateName + ".ftl";
		try {
			cfg.setDirectoryForTemplateLoading(new File(templateDir));
			template = cfg.getTemplate(tName);
			DefaultObjectWrapperBuilder builder = 
					new DefaultObjectWrapperBuilder(cfg.getIncompatibleImprovements());
			cfg.setObjectWrapper(builder.build());
			File op = new File(outputPath);
			if (!op.exists() && !op.mkdirs()) {
					throw new IOException(
							"An error occurred during folder creation " + outputPath);
			}
		} catch (IOException e) {
			throw new IOException("Can't find template " + tName + ".", e);
		}

	}

	public Writer getWriter(String fileNamePart, String packageName) throws IOException {
		if (!Objects.equals(packageName, filePackage)) {
			filePackage = packageName.replace(".", File.separator);
		}
			
		String fullPath = outputPath
				+ File.separator
				+ (filePackage.isEmpty() ? "" : packageToPath(filePackage)
						+ File.separator)
				+ outputFileName.replace("{0}", fileNamePart);

		File of = new File(fullPath);
		if (!of.getParentFile().exists()) {
			if (!of.getParentFile().mkdirs()) {
				throw new IOException("An error occurred during output folder creation "
						+ outputPath);
			}
		}

		System.out.println(of.getPath());
		System.out.println(of.getName());

		if (!isOverwrite() && of.exists()) {
			return null;
		}

		return new OutputStreamWriter(Files.newOutputStream(of.toPath()));

	}

	protected String packageToPath(String pack) {
		return pack.replace(".", File.separator);
	}
}
