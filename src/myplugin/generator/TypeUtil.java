package myplugin.generator;

public class TypeUtil {

    public String toJava(String umlTypeName) {
        if (umlTypeName == null) return "Object";
        String t = umlTypeName.trim().toLowerCase();

        switch (t) {
            case "string": return "String";
            case "int":
            case "integer": return "Integer";
            case "long": return "Long";
            case "double": return "Double";
            case "float": return "Float";
            case "boolean": return "Boolean";
            case "date":
            case "datetime": return "java.time.LocalDateTime";
            case "uuid":
            case "java.util.uuid":
                return "java.util.UUID";
            default:
                return umlTypeName;
        }
    }
}