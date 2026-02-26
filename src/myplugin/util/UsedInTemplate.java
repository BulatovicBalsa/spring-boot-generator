package myplugin.util;

import java.lang.annotation.*;

/**
 * Marker annotation for methods/fields that are referenced from Freemarker templates (.ftl).
 * Helps suppress IDE "unused" warnings.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface UsedInTemplate {
    /**
     * Optional: list template names where this element is used (e.g. "entity.ftl").
     */
    String[] value() default {};
}