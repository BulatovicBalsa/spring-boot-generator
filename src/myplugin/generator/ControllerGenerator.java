package myplugin.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import myplugin.generator.fmmodel.FMClass;
import myplugin.generator.fmmodel.FMModel;
import myplugin.generator.options.GeneratorOptions;

public class ControllerGenerator extends BasicGenerator {

    private final NameUtil nameUtil = new NameUtil();

    private final String entityPackage;
    private final String dtoPackage;
    private final String serviceCrudPackage;

    public ControllerGenerator(GeneratorOptions options, String entityPackage, String dtoPackage, String serviceCrudPackage) {
        super(options);
        this.entityPackage = entityPackage;
        this.dtoPackage = dtoPackage;
        this.serviceCrudPackage = serviceCrudPackage;
    }

    @Override
    public void generate() throws IOException {
        super.generate();

        for (FMClass clazz : FMModel.getInstance().getClasses()) {
            Writer out = getWriter(clazz.getName() + "Controller", generatorOptions.getFilePackage());
            if (out == null) continue;

            String idType = clazz.resolveIdType(generatorOptions);

            String entityFqn = entityPackage + "." + clazz.getName();
            String dtoFqn = dtoPackage + "." + clazz.getName() + "DTO";
            String serviceFqn = serviceCrudPackage + ".I" + clazz.getName() + "ServiceCrud";

            // /api/users, /api/order_items, itd.
            String basePath = "/api/" + nameUtil.toPluralResourceName(clazz.getName());

            Map<String, Object> model = new HashMap<>();
            model.put("packageName", generatorOptions.getFilePackage());
            model.put("clazz", clazz);
            model.put("idType", idType);
            model.put("entityFqn", entityFqn);
            model.put("dtoFqn", dtoFqn);
            model.put("serviceFqn", serviceFqn);
            model.put("basePath", basePath);

            try {
                getTemplate().process(model, out);
            } catch (Exception e) {
                throw new IOException("Template processing failed for controller: " + clazz.getName(), e);
            } finally {
                out.close();
            }
        }
    }
}
