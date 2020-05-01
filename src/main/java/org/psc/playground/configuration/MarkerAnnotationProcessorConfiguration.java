package org.psc.playground.configuration;

import lombok.extern.slf4j.Slf4j;
import org.psc.playground.model.FieldMarker;
import org.psc.playground.model.SubModel;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class MarkerAnnotationProcessorConfiguration {

    @Bean
    public ApplicationRunner classMarkerAnnotationProcessor() {
        return args -> {
            //   Reflections reflections = new Reflections();
            //   Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(ClassMarker.class);
        };
    }

    //    @Bean
    public ApplicationRunner fieldMarkerAnnotationProcessor() {
        return args -> {
            Reflections reflections = new Reflections(new FieldAnnotationsScanner());
            List<Class<?>> typesToProcess = reflections.getFieldsAnnotatedWith(FieldMarker.class)
                    .stream()
                    .map(Field::getDeclaringClass)
                    .distinct()
                    .collect(Collectors.toList());

            typesToProcess.stream()
                    .peek(clazz -> log.info("ClassMarker: {}", clazz.getName()))
                    .forEach(clazz -> {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            try {
                                FieldMarker fieldMarker = fields[i].getDeclaredAnnotation(FieldMarker.class);
                                setAttributeValue(fieldMarker, FieldMarker.class,"position", i + 1);
                                Method annotationDataMethod = fieldMarker
                                        .getClass()
                                        .getDeclaredMethod("annotationData");
                                annotationDataMethod.setAccessible(true);

                                Field annotationData = fields[i].getAnnotation(FieldMarker.class)
                                        .getClass()
                                        .getDeclaredField("annotationData");
                                annotationData.setAccessible(true);

                                Map<Class<? extends Annotation>, Annotation> map =
                                        (Map<Class<? extends Annotation>, Annotation>) annotationData.get(fields[i]);

                                int position = i + 1;
                                map.put(FieldMarker.class, new FieldMarker() {

                                    @Override
                                    public Class<? extends Annotation> annotationType() {
                                        return fieldMarker.getClass();
                                    }

                                    @Override
                                    public String description() {
                                        return fieldMarker.description();
                                    }

                                    @Override
                                    public String name() {
                                        return fieldMarker.name();
                                    }

                                    @Override
                                    public int position() {
                                        return position;
                                    }
                                });

                            } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    });

        };
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    public ApplicationRunner proxyingFieldMarkerAnnotationProcessor() {
        return args -> {
            Reflections reflections = new Reflections(new FieldAnnotationsScanner());
            List<Class<?>> typesToProcess = reflections.getFieldsAnnotatedWith(FieldMarker.class)
                    .stream()
                    .map(Field::getDeclaringClass)
                    .distinct()
                    .collect(Collectors.toList());

            typesToProcess.stream()
                    .peek(clazz -> log.info("ClassMarker: {}", clazz.getName()))
                    .forEach(clazz -> {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            FieldMarker fieldMarker = fields[i].getDeclaredAnnotation(FieldMarker.class);
//                            setAttributeValue(fieldMarker, FieldMarker.class, "position", i + 1);
                            try {
//                                Method method = Class.class.getDeclaredMethod("annotationData");
//                                method.setAccessible(true);
//
//                                Object annotationData = method.invoke(clazz);
//                                Field annotations = annotationData.getClass().getDeclaredField("annotations");
//                                annotations.setAccessible(true);
//
//                                Map<Class<? extends Annotation>, Annotation> annotationsMap =
//                                        (Map<Class<? extends Annotation>, Annotation>) annotations.get(annotationData);

                                Method method = fields[i].getClass().getDeclaredMethod("declaredAnnotations");
                                method.setAccessible(true);

                                Map<Class<? extends Annotation>, Annotation> annotationsMap =
                                        (Map<Class<? extends Annotation>, Annotation>) method.invoke(fields[i]);
                                int position = i + 1;
                                annotationsMap.put(FieldMarker.class, new FieldMarker(){

                                    @Override
                                    public Class<? extends Annotation> annotationType() {
                                        return fieldMarker.annotationType();
                                    }

                                    @Override
                                    public String description() {
                                        return fieldMarker.description();
                                    }

                                    @Override
                                    public String name() {
                                        return fieldMarker.name();
                                    }

                                    @Override
                                    public int position() {
                                        return position;
                                    }
                                });
                            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        };
    }

    @Bean
    public ApplicationRunner validatingApplicationRunner() {
        return args -> {
            Field[] declaredFields = SubModel.class.getDeclaredFields();
            for (Field field : declaredFields) {
                FieldMarker fieldMarker = field.getAnnotation(FieldMarker.class);
                log.info("{} - position = {}", field.getName(), fieldMarker.position());
            }
        };
    }

    public static Annotation setAttributeValue(Annotation annotation, Class<?> annotationType, String attributeName, Object newValue) {
        InvocationHandler handler = new AnnotationInvocationHandler(annotation, attributeName, newValue);
        return (Annotation) Proxy.newProxyInstance(annotation.getClass().getClassLoader(),
                new Class[]{annotationType}, handler);
    }

    public static class AnnotationInvocationHandler implements InvocationHandler {
        private final Annotation original;
        private final String attributeName;
        private final Object newValue;

        public AnnotationInvocationHandler(Annotation original, String attributeName, Object newValue) {
            this.original = original;
            this.attributeName = attributeName;
            this.newValue = newValue;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // "override" the return value for the property we want
            if (method.getName().equals(attributeName) && args == null) {
                return newValue;
            } else {
                // keep other properties and methods we want like equals() and hashCode()
                Class<?>[] parameterTypes = toClassArray(args);
                return original.getClass().getMethod(method.getName(), parameterTypes).invoke(original, args);
            }
        }

        private static Class<?>[] toClassArray(Object[] array) {
            if (array == null) {
                return null;
            }
            return (Class<?>[]) Arrays.stream(array).map(Object::getClass).toArray();
        }

    }
}
