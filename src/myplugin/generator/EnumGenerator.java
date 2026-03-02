package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMEnumeration;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class EnumGenerator extends BasicGenerator {

    public EnumGenerator(GeneratorOptions options) {
        super(options);
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMEnumeration en : FMModel.getInstance().getEnumerations()) {
            Writer out = getWriter(en.getName(), generatorOptions.getFilePackage());
            if (out == null) continue;

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("enum", en);

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for enum: " + en.getName(), e);
            } finally {
                out.close();
            }
        }
    }
}