package org.example.service.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.example.service.utils.annotations.SpecifiedScan;
import org.reflections.ReflectionUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

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

    public Map<String, Object> getFieldsAndValues(T obj) {

        Set<Field> fields = getFieldsToScan();

        if (fields.size() != 0) {

            return getMapOfFieldsAndValues(obj, fields);
        }

        return new HashMap<>();

    }


    public Map<String, Pair<Object, Object>> getChanges(T o1, T o2) {

        Set<Field> fields = getFieldsToScan();

        if (fields.size() != 0) {

            return getMapOfDifferencesOfFields(o1, o2, fields);
        }


        return new HashMap<>();

    }

    private Map<String, Object> getMapOfFieldsAndValues(T obj, Set<Field> fields) {

        Map<String, Object> res = new HashMap<>();

        fields.forEach(
                field -> {
                    if (!field.canAccess(obj)) {
                        try {
                            field.setAccessible(true);

                            mapFieldNameAndValue(obj, res, field);
                        } finally {
                            field.setAccessible(false);
                        }
                    } else {
                        mapFieldNameAndValue(obj, res, field);
                    }

                }

        );

        return res;

    }


    private Map<String, Pair<Object, Object>> getMapOfDifferencesOfFields(T o1, T o2, Set<Field> fields) {

        Map<String, Pair<Object, Object>> res = new HashMap<>();

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

    private void mapFieldNameAndValue(T obj, Map<String, Object> res, Field field) {

        try {

            Object value = field.get(obj);

            Annotation[] annotations = field.getAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof SpecifiedScan) {
                    mapFieldNameAndValueForSpecifiedScanAnnotation(obj, res, field, (SpecifiedScan) annotation);
                    return;
                }
            }


            String fieldName = field.getName();

            if (null==value) {
                res.put(fieldName, "null");
            } else if (fieldsWithNoValuesToDisclose.contains(fieldName)){
                res.put(fieldName, null);
            } else {
                res.put(fieldName, value);
            }

        } catch ( IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    private void compareTwoFields(T o1, T o2, Map<String, Pair<Object, Object>> res, Field field) {

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
                        res.put(fieldName, Pair.of(val1, val2));

                    } else {
                        res.put(fieldName, null);
                    }

                }

                return;

            }

            if (null == val1) {
                String fieldName = field.getName();
                if (!fieldsWithNoValuesToDisclose.contains(fieldName)) {
                    res.put(fieldName, Pair.of("null", val2));
                } else {
                    res.put(fieldName, null);
                }
                return;

            }

            if (null == val2) {
                String fieldName = field.getName();
                if (!fieldsWithNoValuesToDisclose.contains(fieldName)) {
                    res.put(fieldName, Pair.of(val1, "null"));
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
                        if (notToScanAnnotations.contains(annotation.annotationType()))
                            return false;
                    }
                    return true;
                }

        );
    }

    private Set<Field> getFieldsToScan(Object obj, String[] fieldsToScan) {
        return ReflectionUtils.get(
                Fields.of(obj.getClass()),
                field -> {

                    for (String fieldToScan : fieldsToScan) {
                        if (field.getName().equals(fieldToScan))
                            return true;

                    }
                    return false;
                }

        );
    }

    private <A extends SpecifiedScan> void mapFieldNameAndValueForSpecifiedScanAnnotation(T obj, Map<String, Object> res, Field fieldToAnalyze, A annotation) throws IllegalAccessException {

        Object value = fieldToAnalyze.get(obj);

        String fieldName = fieldToAnalyze.getName();

        String[] fieldsToScan = annotation.fieldsToScan();

        Set<Field> fields = getFieldsToScan(value, fieldsToScan);

        Map<String, Object> innerMap = new HashMap<>();

        for (Field field : fields) {

            if (!field.canAccess(value)) {

                try {
                    field.setAccessible(true);

                    innerMap.put(field.getName(), field.get(value));

                } finally {
                    field.setAccessible(false);
                }

            } else {

                    innerMap.put(field.getName(), field.get(value));

            }

        }

        res.put(fieldName, innerMap.size() == 0 ? "n/a" : innerMap);

    }

    public static class Builder<T> {

        private Class<T> targetClass;
        private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

        private Set<Class<? extends Annotation>> notToScanAnnotations = new HashSet<>();

        private Set<String> fieldsWithNoValuesToDisclose = new HashSet<>();

        public Builder(Class<T> targetClass) {
            this.targetClass = targetClass;
        }


        public Builder<T> setNotToScanAnnotations(Collection<Class<? extends Annotation>> classes) {
            this.notToScanAnnotations.addAll(classes);
            return this;
        }

        public Builder<T> setFieldsWithNoValuesToDisclose(Collection<String> namesOfFieldsNotToDisclose) {
            this.fieldsWithNoValuesToDisclose.addAll(namesOfFieldsNotToDisclose);
            return this;
        }

        public ChangedFieldsOfEntitySearcher<T> build() {
            return new ChangedFieldsOfEntitySearcher<T>(
                    targetClass, notToScanAnnotations, fieldsWithNoValuesToDisclose
            );
        }
    }
}
