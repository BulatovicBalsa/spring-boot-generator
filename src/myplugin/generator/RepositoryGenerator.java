package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class RepositoryGenerator extends BasicGenerator {

    private final TypeUtil typeUtil = new TypeUtil();

    public RepositoryGenerator(GeneratorOptions options) {
        super(options);
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            Writer out = getWriter(clazz.getName() + "Repository", generatorOptions.getFilePackage());
            if (out == null) continue;

            String idType = clazz.resolveIdType(generatorOptions);

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("idType", idType);
            model.put("typeUtil", typeUtil);

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for repository: " + clazz.getName(), e);
            } finally {
                out.close();
            }
        }
    }
}