package myplugin.generator;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import myplugin.MyPlugin;
import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class RepositoryGenerator extends BasicGenerator {

    private static final String IMPORTS_START = "// <protected:imports>";
    private static final String IMPORTS_END = "// </protected:imports>";
    private static final String METHODS_START = "// <protected:methods>";
    private static final String METHODS_END = "// </protected:methods>";

    private final TypeUtil typeUtil = new TypeUtil();

    public RepositoryGenerator(GeneratorOptions options) {
        super(options);
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            if(clazz.isEmbeddable()) continue;
            Path outputPath = resolveOutputPath(clazz.getName() + "Repository", generatorOptions.getFilePackage());
            if (!generatorOptions.getOverwrite() && Files.exists(outputPath)) {
                continue;
            }

            String idType = clazz.resolveIdType(generatorOptions);

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("idType", idType);
            model.put("typeUtil", typeUtil);
            model.put("entityPackage", MyPlugin.ENTITY_OPTIONS.getFilePackage());

            try {
                String generatedContent = renderTemplate(model);
                String finalContent = generatedContent;

                if (Files.exists(outputPath)) {
                    String existingContent = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
                    try {
                        String importsBlock = extractProtectedContent(existingContent, IMPORTS_START, IMPORTS_END);
                        String methodsBlock = extractProtectedContent(existingContent, METHODS_START, METHODS_END);
                        finalContent = replaceProtectedContent(finalContent, IMPORTS_START, IMPORTS_END, importsBlock);
                        finalContent = replaceProtectedContent(finalContent, METHODS_START, METHODS_END, methodsBlock);
                    } catch (IllegalStateException e) {
                        System.err.println("Skipping overwrite for malformed protected areas in: " + outputPath);
                        continue;
                    }
                }

                Path parent = outputPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.write(outputPath, finalContent.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new IOException("Template processing failed for repository: " + clazz.getName(), e);
            }
        }
    }

    private String renderTemplate(Map<String, Object> model) throws Exception {
        StringWriter out = new StringWriter();
        getTemplate().process(model, out);
        return out.toString();
    }

    private Path resolveOutputPath(String fileNamePart, String packageName) {
        String filePackage = generatorOptions.getFilePackage();
        if (!Objects.equals(packageName, filePackage)) {
            filePackage = packageName.replace(".", File.separator);
        }

        String fullPath = generatorOptions.getOutputPath()
                + File.separator
                + (filePackage.isEmpty() ? "" : packageToPath(filePackage)
                        + File.separator)
                + generatorOptions.getOutputFileName().replace("{0}", fileNamePart);

        return Paths.get(fullPath);
    }

    private String extractProtectedContent(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        int end = source.indexOf(endMarker);

        if (start < 0 && end < 0) {
            return "";
        }
        if (start < 0 || end < 0 || end < start) {
            throw new IllegalStateException("Malformed protected block");
        }

        int contentStart = start + startMarker.length();
        return source.substring(contentStart, end);
    }

    private String replaceProtectedContent(String target, String startMarker, String endMarker, String preservedContent) {
        int start = target.indexOf(startMarker);
        int end = target.indexOf(endMarker);

        if (start < 0 || end < 0 || end < start) {
            throw new IllegalStateException("Protected markers not found in template output");
        }

        int contentStart = start + startMarker.length();
        return target.substring(0, contentStart)
                + preservedContent
                + target.substring(end);
    }
}