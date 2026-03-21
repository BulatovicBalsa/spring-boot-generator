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

	private Configuration cfg;
	private Template template;
	protected final GeneratorOptions generatorOptions;

	public BasicGenerator(GeneratorOptions generatorOptions) {
		this.generatorOptions = generatorOptions;
	}

	public void generate() throws IOException {
		if (generatorOptions.getOutputPath() == null) {
			throw new IOException("Output path is not defined!");
		}	
		if (generatorOptions.getTemplateName() == null) {
			throw new IOException("Template name is not defined!");
		}
		if (generatorOptions.getOutputFileName() == null) {
			throw new IOException("Output file name is not defined!");
		}
		if (generatorOptions.getFilePackage() == null) {
			throw new IOException("Package name for code generation is not defined!");
		}

		cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);		

		final String tName = generatorOptions.getTemplateName() + ".ftl";
		try {
			cfg.setDirectoryForTemplateLoading(new File(generatorOptions.getTemplateDir()));
			template = cfg.getTemplate(tName);
			DefaultObjectWrapperBuilder builder = 
					new DefaultObjectWrapperBuilder(cfg.getIncompatibleImprovements());
			cfg.setObjectWrapper(builder.build());
			File op = new File(generatorOptions.getOutputPath());
			if (!op.exists() && !op.mkdirs()) {
					throw new IOException(
							"An error occurred during folder creation " + generatorOptions.getOutputPath());
			}
		} catch (IOException e) {
			throw new IOException("Template error [" + tName + "]: " + e.getMessage(), e);
		}

	}

	public Writer getWriter(String fileNamePart, String packageName) throws IOException {
		String filePackage = generatorOptions.getFilePackage();
		if (!Objects.equals(packageName, filePackage)) {
			filePackage = packageName.replace(".", File.separator);
		}
			
		String fullPath = generatorOptions.getOutputPath()
				+ File.separator
				+ (filePackage.isEmpty() ? "" : packageToPath(filePackage)
						+ File.separator)
				+ generatorOptions.getOutputFileName().replace("{0}", fileNamePart);

		File of = new File(fullPath);
		if (!of.getParentFile().exists()) {
			if (!of.getParentFile().mkdirs()) {
				throw new IOException("An error occurred during output folder creation "
						+ generatorOptions.getOutputPath());
			}
		}

		System.out.println(of.getPath());
		System.out.println(of.getName());

		if (!generatorOptions.getOverwrite() && of.exists()) {
			return null;
		}

		return new OutputStreamWriter(Files.newOutputStream(of.toPath()));

	}

	protected String packageToPath(String pack) {
		return pack.replace(".", File.separator);
	}
}
