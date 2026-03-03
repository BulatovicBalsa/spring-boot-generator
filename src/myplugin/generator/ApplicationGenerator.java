package myplugin.generator;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import myplugin.generator.options.GeneratorOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ApplicationGenerator extends BasicGenerator {

    public ApplicationGenerator(GeneratorOptions options) {
        super(options);
    }

    public void generate() throws IOException {

        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setDirectoryForTemplateLoading(new File(generatorOptions.getTemplateDir()));
        cfg.setObjectWrapper(
                new DefaultObjectWrapperBuilder(cfg.getIncompatibleImprovements()).build()
        );

        Template template = cfg.getTemplate("application.ftl");

        String packagePath = generatorOptions.getFilePackage().replace('.', File.separatorChar);
        File outDir = new File(generatorOptions.getOutputPath(), packagePath);

        if (!outDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }

        File outFile = new File(outDir, "Application.java");

        if (outFile.exists() && !generatorOptions.getOverwrite()) {
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("basePackage", generatorOptions.getFilePackage());

        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outFile.toPath()), StandardCharsets.UTF_8))) {
            template.process(model, out);
        } catch (Exception e) {
            throw new IOException("Failed to generate application class", e);
        }
    }
}