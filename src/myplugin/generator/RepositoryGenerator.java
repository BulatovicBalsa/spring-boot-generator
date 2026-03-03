package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.fmmodel.FMProperty;
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

            String idType = resolveIdType(clazz);

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

    /**
     * Pravilo:
     * 1) Ako klasa ima ID property sa tipom -> koristi taj tip (mapiran u Java tip)
     * 2) Ako nema -> koristi generatorOptions.idStrategy (UUID ili Long)
     */
    private String resolveIdType(FMClass clazz) {
        FMProperty idProp = findIdProperty(clazz);
        if (idProp != null && idProp.getType() != null) {
            String javaType = typeUtil.toJava(idProp.getType());
            // TypeUtil vraća "java.util.UUID" ili "Long" ili sl.
            if ("java.util.UUID".equals(javaType)) return "UUID";
            // ako vrati fully qualified LocalDateTime itd, repo ID ne treba to, ali ostavljamo:
            if (javaType.indexOf('.') >= 0) {
                // fallback: uzmi simple name
                return javaType.substring(javaType.lastIndexOf('.') + 1);
            }
            return javaType;
        }

        // fallback po strategiji
        if (generatorOptions.getIdStrategy() == IdStrategy.UUID) return "UUID";
        return "Long";
    }

    private FMProperty findIdProperty(FMClass clazz) {
        if (clazz == null || clazz.getProperties() == null) return null;
        for (FMProperty p : clazz.getProperties()) {
            if (p != null && p.isId()) return p;
        }
        return null;
    }
}