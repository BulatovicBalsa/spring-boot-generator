package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.fmmodel.FMProperty;
import myplugin.generator.options.GeneratorOptions;

public class ServiceCrudGenerator extends BasicGenerator {

    private final TypeUtil typeUtil = new TypeUtil();

    private final String entityPackage;
    private final String repositoryPackage;

    public ServiceCrudGenerator(GeneratorOptions options, String entityPackage, String repositoryPackage) {
        super(options);
        this.entityPackage = entityPackage;
        this.repositoryPackage = repositoryPackage;
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        boolean generatingInterface = isInterfaceTemplate(generatorOptions.getTemplateName());

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            String idType = resolveIdType(clazz);

            String fileNamePart;
            if (generatingInterface) {
                fileNamePart = "I" + clazz.getName() + "ServiceCrud";
            } else {
                fileNamePart = clazz.getName() + "ServiceCrudImpl";
            }

            Writer out = getWriter(fileNamePart, generatorOptions.getFilePackage());
            if (out == null) continue;

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("idType", idType);

            String entityFqn = entityPackage + "." + clazz.getName();
            String repoFqn = repositoryPackage + "." + clazz.getName() + "Repository";
            model.put("entityFqn", entityFqn);
            model.put("repoFqn", repoFqn);

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for service CRUD: " + clazz.getName(), e);
            } finally {
                out.close();
            }
        }
    }

    private boolean isInterfaceTemplate(String templateName) {
        return "service-crud".equals(templateName);
    }

    private String resolveIdType(FMClass clazz) {
        FMProperty idProp = findIdProperty(clazz);
        if (idProp != null && idProp.getType() != null) {
            String javaType = typeUtil.toJava(idProp.getType());
            if ("java.util.UUID".equals(javaType)) return "UUID";
            if (javaType.indexOf('.') >= 0) {
                return javaType.substring(javaType.lastIndexOf('.') + 1);
            }
            return javaType;
        }

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