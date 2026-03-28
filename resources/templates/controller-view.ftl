package ${packageName};

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ${entityFqn};
import ${dtoFqn};
import ${serviceFqn};

@Controller
@RequestMapping("${basePath}")
public class ${clazz.name}PageController {

    private final I${clazz.name}ServiceCrud service;

    public ${clazz.name}PageController(I${clazz.name}ServiceCrud service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        List<${clazz.name}DTO> items = new ArrayList<${clazz.name}DTO>();
        for (${clazz.name} entity : service.findAll()) {
            items.add(new ${clazz.name}DTO(entity));
        }

        List<String> columns = new ArrayList<String>();
        for (java.lang.reflect.Field field : ${clazz.name}DTO.class.getDeclaredFields()) {
            columns.add(field.getName());
        }

        List<Map<String, Object>> tableRows = new ArrayList<Map<String, Object>>();

        for (${clazz.name}DTO dto : items) {
            Map<String, Object> row = toMap(dto);
            List<Object> cells = new ArrayList<Object>();
            for (String col : columns) {
                cells.add(row.get(col));
            }

            Map<String, Object> tableRow = new LinkedHashMap<String, Object>();
            tableRow.put("id", row.get("id"));
            tableRow.put("cells", cells);
            tableRows.add(tableRow);
        }

        model.addAttribute("tableRows", tableRows);
        model.addAttribute("columns", columns);
        model.addAttribute("entityName", "${clazz.name}");
        model.addAttribute("basePath", "${basePath}");
        return "crud/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable ${idType} id, Model model) {
        ${clazz.name} found = service.findById(id);
        if (found == null) {
            model.addAttribute("entityName", "${clazz.name}");
            model.addAttribute("id", id);
            return "crud/not-found";
        }

        ${clazz.name}DTO dto = new ${clazz.name}DTO(found);
        model.addAttribute("entry", toMap(dto));
        model.addAttribute("entityName", "${clazz.name}");
        model.addAttribute("basePath", "${basePath}");
        return "crud/details";
    }

    private Map<String, Object> toMap(${clazz.name}DTO dto) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (java.lang.reflect.Field field : ${clazz.name}DTO.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                map.put(field.getName(), normalizeValue(value));
            } catch (IllegalAccessException ignored) {
                map.put(field.getName(), null);
            }
        }
        return map;
    }

    private Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value.getClass().isEnum()) {
            return value;
        }
        if (value instanceof java.util.UUID || value instanceof java.time.temporal.Temporal) {
            return value.toString();
        }
        if (value instanceof java.util.Collection) {
            return "Collection(size=" + ((java.util.Collection<?>) value).size() + ")";
        }
        try {
            java.lang.reflect.Method getId = value.getClass().getMethod("getId");
            Object id = getId.invoke(value);
            return value.getClass().getSimpleName() + "#" + id;
        } catch (Exception ignored) {
            return value.toString();
        }
    }
}
