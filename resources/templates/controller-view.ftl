package ${packageName};

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ${entityFqn};
import ${dtoFqn};
import ${serviceFqn};

@Controller
@RequestMapping("${basePath}")
public class ${clazz.name}PageController {

    private final I${clazz.name}ServiceCrud service;
    private final ApplicationContext applicationContext;

    public ${clazz.name}PageController(I${clazz.name}ServiceCrud service, ApplicationContext applicationContext) {
        this.service = service;
        this.applicationContext = applicationContext;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public String details(@PathVariable ${idType} id, Model model) {
        ${clazz.name} found = service.findById(id);
        if (found == null) {
            model.addAttribute("entityName", "${clazz.name}");
            model.addAttribute("id", id);
            model.addAttribute("basePath", "${basePath}");
            return "crud/not-found";
        }

        ${clazz.name}DTO dto = new ${clazz.name}DTO(found);
        model.addAttribute("entry", toMap(dto));
        model.addAttribute("entityName", "${clazz.name}");
        model.addAttribute("basePath", "${basePath}");
        return "crud/details";
    }

    @Transactional(readOnly = true)
    @GetMapping("/new")
    public String createForm(Model model) {
        ${clazz.name}DTO dto = new ${clazz.name}DTO();
        model.addAttribute("entityName", "${clazz.name}");
        model.addAttribute("basePath", "${basePath}");
        model.addAttribute("formTitle", "Create ${clazz.name}");
        model.addAttribute("formAction", "${basePath}");
        model.addAttribute("fields", buildFormFields(dto));
        return "crud/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> formData, RedirectAttributes redirectAttributes) {
        try {
            ${clazz.name}DTO dto = mapToDto(formData);
            service.create(new ${clazz.name}(dto));
            redirectAttributes.addFlashAttribute("flashMessage", "Created successfully.");
            return "redirect:${basePath}";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("flashError", "Create failed: " + e.getMessage());
            return "redirect:${basePath}/new";
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable ${idType} id, Model model) {
        ${clazz.name} found = service.findById(id);
        if (found == null) {
            model.addAttribute("entityName", "${clazz.name}");
            model.addAttribute("id", id);
            model.addAttribute("basePath", "${basePath}");
            return "crud/not-found";
        }

        ${clazz.name}DTO dto = new ${clazz.name}DTO(found);
        model.addAttribute("entityName", "${clazz.name}");
        model.addAttribute("basePath", "${basePath}");
        model.addAttribute("formTitle", "Edit ${clazz.name}");
        model.addAttribute("formAction", "${basePath}/" + id);
        model.addAttribute("fields", buildFormFields(dto));
        model.addAttribute("entityId", id);
        return "crud/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable ${idType} id, @RequestParam Map<String, String> formData, RedirectAttributes redirectAttributes) {
        ${clazz.name} existing = service.findById(id);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("flashError", "Entity not found.");
            return "redirect:${basePath}";
        }

        try {
            ${clazz.name}DTO dto = mapToDto(formData);
            setIdOnDto(dto, id);
            existing.updateFromDto(dto);
            service.update(existing);
            redirectAttributes.addFlashAttribute("flashMessage", "Updated successfully.");
            return "redirect:${basePath}";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("flashError", "Update failed: " + e.getMessage());
            return "redirect:${basePath}/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable ${idType} id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("flashMessage", "Deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("flashError", "Delete failed: " + e.getMessage());
        }
        return "redirect:${basePath}";
    }

    private List<Map<String, Object>> getRelationshipOptions(Class<?> dtoFieldType) {
        List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        try {
            String dtoTypeName = dtoFieldType.getSimpleName();
            String entityName = dtoTypeName.endsWith("DTO")
            ? dtoTypeName.substring(0, dtoTypeName.length() - 3)
            : dtoTypeName;

            Object matchedService = null;
            try {
                String beanName = entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "ServiceCrudImpl";
                matchedService = applicationContext.getBean(beanName);
            } catch (Exception e) {
                return options;
            }

            if (matchedService == null) {
                return options;
            }

            java.lang.reflect.Method findAll = matchedService.getClass().getMethod("findAll");
            List<?> entities = (List<?>) findAll.invoke(matchedService);

            for (Object entity : entities) {
                Map<String, Object> option = new LinkedHashMap<String, Object>();

                java.lang.reflect.Method getId = entity.getClass().getMethod("getId");
                option.put("id", getId.invoke(entity));

                option.put("label", getFirstNonIdFieldValue(entity));

                options.add(option);
            }
        } catch (Exception e) { }
        return options;
    }

    private String getFirstNonIdFieldValue(Object entity) {
        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            if ("id".equals(field.getName())) continue;
            if (!isSimpleEditableType(field.getType())) continue;
            field.setAccessible(true);
            try {
                Object val = field.get(entity);
                if (val != null) return val.toString();
            } catch (IllegalAccessException ignored) {}
        }

        try {
            java.lang.reflect.Method getId = entity.getClass().getMethod("getId");
            return entity.getClass().getSimpleName() + "#" + getId.invoke(entity);
        } catch (Exception ignored) {}
        return entity.toString();
    }

    private Map<String, Object> toMap(${clazz.name}DTO dto) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (java.lang.reflect.Field field : ${clazz.name}DTO.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                map.put(field.getName(), normalizeValue(field, value));
            } catch (IllegalAccessException ignored) {
                map.put(field.getName(), null);
            }
        }
        return map;
    }

    private Object normalizeValue(java.lang.reflect.Field field, Object value) {
        if (value == null) return null;

        if (value instanceof java.util.Collection) {
            if (org.hibernate.Hibernate.isInitialized(value)) {
                return "Count: " + ((java.util.Collection<?>) value).size();
            }

            return "Count: ?";
        }

        String typeName = field.getType().getSimpleName();
        if (typeName.endsWith("DTO") && !isSimpleEditableType(field.getType())) {
            return getFirstNonIdFieldValue(value);
        }

        if (value instanceof String || value instanceof Number || value instanceof Boolean
            || value.getClass().isEnum()) {
            return value;
        }

        if (value instanceof java.util.UUID || value instanceof java.time.temporal.Temporal) {
            return value.toString();
        }

        return value.toString();
    }

    private List<Map<String, Object>> buildFormFields(${clazz.name}DTO dto) {
        Map<String, Object> values = toMap(dto);
        List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
        for (java.lang.reflect.Field field : ${clazz.name}DTO.class.getDeclaredFields()) {
            String name = field.getName();
            if ("id".equals(name)) {
                continue;
            }
            Class<?> type = field.getType();
            String typeName = type.getSimpleName();

            Map<String, Object> f = new LinkedHashMap<String, Object>();
            f.put("name", name);
            f.put("label", toLabel(name));

            if (typeName.endsWith("DTO") && !isSimpleEditableType(type)) {
                List<Map<String, Object>> options = getRelationshipOptions(type);
                f.put("type", "select");
                f.put("options", options);

                Object currentDto = null;
                field.setAccessible(true);
                try { currentDto = field.get(dto); } catch (IllegalAccessException ignored) {}
                Object selectedId = null;
                if (currentDto != null) {
                    try {
                        java.lang.reflect.Field idField = currentDto.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        selectedId = idField.get(currentDto);
                    } catch (Exception ignored) {}
                }
                f.put("selectedId", selectedId == null ? "" : selectedId.toString());
                fields.add(f);
                continue;
            }

            if (java.util.Collection.class.isAssignableFrom(type)) {
                continue;
            }

            if (!isSimpleEditableType(type)) continue;

            f.put("type", "text");

            Object value = values.get(name);
            f.put("value", value == null ? "" : value.toString());
            fields.add(f);
        }
        return fields;
    }

    private ${clazz.name}DTO mapToDto(Map<String, String> formData) throws Exception {
        ${clazz.name}DTO dto = new ${clazz.name}DTO();
        for (java.lang.reflect.Field field : ${clazz.name}DTO.class.getDeclaredFields()) {
            String name = field.getName();
            if ("id".equals(name)) {
                continue;
            }
            Class<?> type = field.getType();
            String typeName = type.getSimpleName();

            if (typeName.endsWith("DTO") && !isSimpleEditableType(type)) {
                if (!formData.containsKey(name)) continue;
                String rawId = formData.get(name);
                if (rawId == null || rawId.trim().isEmpty()) continue;

                Object relatedDto = type.getDeclaredConstructor().newInstance();
                try {
                    java.lang.reflect.Field idField = type.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(relatedDto, parseValue(idField.getType(), rawId));
                } catch (NoSuchFieldException ignored) {}
                    field.setAccessible(true);
                    field.set(dto, relatedDto);
                    continue;
            }

            if (java.util.Collection.class.isAssignableFrom(type)) continue;
            if (!isSimpleEditableType(type)) continue;
            if (!formData.containsKey(name)) continue;

            field.setAccessible(true);
            Object value = parseValue(type, formData.get(name));
            field.set(dto, value);
        }
        return dto;
    }

    private void setIdOnDto(${clazz.name}DTO dto, ${idType} id) throws Exception {
        java.lang.reflect.Field idField = ${clazz.name}DTO.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(dto, id);
    }

    private boolean isSimpleEditableType(Class<?> type) {
        return type == String.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Double.class || type == double.class
                || type == Float.class || type == float.class
                || type == Short.class || type == short.class
                || type == Byte.class || type == byte.class
                || type == Boolean.class || type == boolean.class
                || type == BigDecimal.class
                || type == LocalDate.class
                || type == java.util.UUID.class
                || type.isEnum();
    }

    private Object parseValue(Class<?> type, String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            if (type.isPrimitive()) {
                if (type == boolean.class) return false;
                if (type == char.class) return Character.MIN_VALUE;
                return 0;
            }
            return null;
        }

        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.valueOf(value);
        if (type == Long.class || type == long.class) return Long.valueOf(value);
        if (type == Double.class || type == double.class) return Double.valueOf(value);
        if (type == Float.class || type == float.class) return Float.valueOf(value);
        if (type == Short.class || type == short.class) return Short.valueOf(value);
        if (type == Byte.class || type == byte.class) return Byte.valueOf(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(value);
        if (type == BigDecimal.class) return new BigDecimal(value);
        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == java.util.UUID.class) return java.util.UUID.fromString(value);
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);
        return null;
    }

    private String toLabel(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(' ').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
