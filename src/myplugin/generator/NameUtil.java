package myplugin.generator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import myplugin.util.UsedInTemplate;

public class NameUtil {

    private static final Set<String> RESERVED_SQL_IDENTIFIERS = new HashSet<String>(Arrays.asList(
            "user", "order", "group", "select", "table", "where", "from", "to", "by",
            "primary", "foreign", "constraint", "index", "column", "insert", "update",
            "delete", "join", "references", "check", "default", "null", "key"
    ));

    @UsedInTemplate({"entity.ftl"})
    public String toSnakeCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append('_');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    @UsedInTemplate({"entity.ftl"})
    public String toTableName(String s) {
        return ensureSafeIdentifier(toSnakeCase(s), "_entity");
    }

    @UsedInTemplate({"entity.ftl"})
    public String toColumnName(String s) {
        return ensureSafeIdentifier(toSnakeCase(s), "_field");
    }

    private String ensureSafeIdentifier(String identifier, String suffix) {
        if (identifier == null || identifier.isEmpty()) return identifier;
        if (RESERVED_SQL_IDENTIFIERS.contains(identifier)) {
            return identifier + suffix;
        }
        return identifier;
    }
}
