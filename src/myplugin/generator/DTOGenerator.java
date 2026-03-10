package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.MyPlugin;
import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class DTOGenerator extends BasicGenerator {

    private final TypeUtil typeUtil = new TypeUtil();

    public DTOGenerator(GeneratorOptions options) {
        super(options);
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            Writer out = getWriter(clazz.getName() + "DTO", generatorOptions.getFilePackage());
            if (out == null) continue;

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("props", clazz.getProperties());
            model.put("typeUtil", typeUtil);
            model.put("entityFqn", MyPlugin.ENTITY_OPTIONS.getFilePackage() + "." + clazz.getName());
            model.put("dtoPackage", generatorOptions.getFilePackage());
            model.put("hasId", clazz.hasId());
            model.put("idType", clazz.resolveIdType(generatorOptions));

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for DTO: " + clazz.getName(), e);
            } finally {
                out.close();
            }
        }
    }
}
