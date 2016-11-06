/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyclonite on 13/11/15.
 */
public class ModelHelper {
    public static List<String> getQueryableFields(final Class<?> objectType, final Class<? extends Annotation> annotationType) {
        final List<String> fields = new ArrayList<>();
        for(final Field field : objectType.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationType)) {
                final String fieldName = field.getName();
                fields.add(fieldName);
            }
        }
        return fields;
    }
}
