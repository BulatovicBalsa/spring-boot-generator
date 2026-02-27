package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class EntityGenerator extends BasicGenerator {

    private final TypeUtil typeUtil = new TypeUtil();
    private final NameUtil nameUtil = new NameUtil();

    public EntityGenerator(GeneratorOptions options) {
        super(options);
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            Writer out = getWriter(clazz.getName(), generatorOptions.getFilePackage());
            if (out == null) continue; 

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("props", clazz.getProperties());
            model.put("typeUtil", typeUtil);
            model.put("hasId", clazz.hasId());
            model.put("idStrategy", generatorOptions.getIdStrategy());
            model.put("nameUtil", nameUtil);

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for class: " + clazz.getName(), e);
            } finally {
                out.close();
            }
        }
    }
}