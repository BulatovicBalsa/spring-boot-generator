package myplugin.generator;

public class StringUtil {

    public static String capitalize(String str) {
        if (isNullOrEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String decapitalize(String str) {
        if (isNullOrEmpty(str)) return str;
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String pluralizeSimple(String str) {
        if (isNullOrEmpty(str)) return str;
        if (str.endsWith("s")) return str;
        return str + "s";
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
