package org.example.service.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static org.reflections.ReflectionUtils.Fields;

public class ChangedFieldsOfEntitySearcher<T> {

    private Class<T> targetClass;

    private Set<Class<? extends Annotation>> notToScanAnnotations;

    private Set<String> fieldsWithNoValuesToDisclose;


    private ChangedFieldsOfEntitySearcher(Class<T> targetClass,
                                          Set<Class<? extends Annotation>> notToScanAnnotations,
                                          Set<String> fieldsWithNoValuesToDisclose) {
        this.targetClass = targetClass;
        this.notToScanAnnotations = notToScanAnnotations;
        this.fieldsWithNoValuesToDisclose = fieldsWithNoValuesToDisclose;
    }


    public Map<String, Pair<String, String>> getChanges(T o1, T o2) {

        Set<Field> fields = getFieldsToScan();

        if (fields.size() != 0) {

            return getMapOfDifferencesOfFields(o1, o2, fields);
        }


        return new HashMap<>();

    }

    private Map<String, Pair<String, String>> getMapOfDifferencesOfFields(T o1, T o2, Set<Field> fields) {
        Map<String, Pair<String, String>> res = new HashMap<>();

        fields.forEach(
                field -> {
                    if (!field.canAccess(o1) || !field.canAccess(o2)) {
                        try {
                            field.setAccessible(true);

                            compareTwoFields(o1, o2, res, field);
                        } finally {
                            field.setAccessible(false);
                        }
                    } else {
                        compareTwoFields(o1, o2, res, field);
                    }

                }

        );

        return res;
    }

    private void compareTwoFields(T o1, T o2, Map<String, Pair<String, String>> res, Field field) {

        try {

            Object val1 = field.get(o1);
            Object val2 = field.get(o2);

            if (val1 == null && val2 == null) {
                return;
            }

            if (null != val1 && null != val2) {

                if (!val1.equals(val2)) {

                    String fieldName = field.getName();


                    if (!fieldsWithNoValuesToDisclose.contains(fieldName)) {
                        res.put(fieldName, Pair.of(val1.toString(), val2.toString()));

                    } else {
                        res.put(fieldName, null);
                    }

                }

                return;

            }


            if (null == val1) {
                String fieldName = field.getName();
                if (!fieldsWithNoValuesToDisclose.contains(fieldName)) {
                    res.put(fieldName, Pair.of("null", val2.toString()));
                } else {
                    res.put(fieldName, null);
                }
                return;

            }

            if (null == val2) {
                String fieldName = field.getName();
                if (!fieldsWithNoValuesToDisclose.contains(fieldName)) {
                    res.put(fieldName, Pair.of(val1.toString(), "null"));
                } else {
                    res.put(fieldName, null);
                }

            }


        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }


    private Set<Field> getFieldsToScan() {
        return ReflectionUtils.get(
                Fields.of(targetClass),
                field -> {
                    Annotation[] annotations = field.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (
                                notToScanAnnotations.contains(annotation.annotationType())
                        )
                            return false;
                    }
                    return true;
                }

        );
    }


    public static class Builder<T> {

        private Class<T> targetClass;

        private Set<Class<? extends Annotation>> notToScanAnnotations = new HashSet<>();

        private Set<String> fieldsWithNoValuesToDisclose = new HashSet<>();

        public Builder(Class<T> targetClass) {
            this.targetClass = targetClass;
        }


        public Builder<T> setNotToScanAnnotations(Collection<Class<? extends Annotation>> classes) {
            this.notToScanAnnotations.addAll(classes);
            return this;
        }

        public Builder<T> setFieldsWithNoValuesToDisclose(Collection<String> namesOfFiledNotToDisclose) {
            this.fieldsWithNoValuesToDisclose.addAll(namesOfFiledNotToDisclose);
            return this;
        }

        public ChangedFieldsOfEntitySearcher<T> build() {
            return new ChangedFieldsOfEntitySearcher<T>(
                    targetClass, notToScanAnnotations, fieldsWithNoValuesToDisclose
            );
        }
    }
}
