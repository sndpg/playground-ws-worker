package org.psc.playground.configuration;

import lombok.extern.slf4j.Slf4j;
import org.psc.playground.model.BaseModel;
import org.psc.playground.model.ClassMarker;
import org.psc.playground.model.FieldMarker;
import org.psc.playground.model.SubModel;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class MarkerAnnotationProcessorConfiguration {

    private static final Constructor<?> ANNOTATION_DATA_CONSTRUCTOR;
    private static final VarHandle ANNOTATION_DATA;
    private static final VarHandle DECLARED_ANNOTATIONS;
    private static final VarHandle ANNOTATIONS;
    private static final VarHandle REDEFINED_COUNT;

    private static final VarHandle ROOT_FIELD;

    private static final Method ANNOTATION_DATA_METHOD;
    private static final Method ATOMIC_CAS_ANNOTATION_DATA_METHOD;

    private static final Class<?> ATOMIC_CLASS;

    static {
        try {
            ATOMIC_CLASS = Class.forName("java.lang.Class$Atomic");

            Class<?> annotationDataClass = Class.forName("java.lang.Class$AnnotationData");
            ANNOTATION_DATA_CONSTRUCTOR = annotationDataClass.getDeclaredConstructor(Map.class, Map.class, int.class);
            ANNOTATION_DATA_CONSTRUCTOR.setAccessible(true);

            ATOMIC_CAS_ANNOTATION_DATA_METHOD = ATOMIC_CLASS.getDeclaredMethod("casAnnotationData",
                    Class.class, annotationDataClass, annotationDataClass);
            ATOMIC_CAS_ANNOTATION_DATA_METHOD.setAccessible(true);

            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            DECLARED_ANNOTATIONS = lookup.findVarHandle(Field.class, "declaredAnnotations", Map.class);
            ANNOTATIONS = lookup.findVarHandle(Field.class, "annotations", byte[].class);
            ROOT_FIELD = lookup.findVarHandle(Field.class, "root", Field.class);

            MethodHandles.Lookup classLookup = MethodHandles.privateLookupIn(Class.class, MethodHandles.lookup());
            REDEFINED_COUNT = classLookup.findVarHandle(Class.class, "classRedefinedCount", int.class);
            ANNOTATION_DATA = classLookup.findVarHandle(Class.class, "annotationData", annotationDataClass);

            ANNOTATION_DATA_METHOD = Class.class.getDeclaredMethod("annotationData");
            ANNOTATION_DATA_METHOD.setAccessible(true);

        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

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
            List<Class<?>> typesWithMarkedFields = reflections.getFieldsAnnotatedWith(FieldMarker.class)
                    .stream()
                    .map(Field::getDeclaringClass)
                    .distinct()
                    .collect(Collectors.toList());

            Set<Class<?>> markedTypes = reflections.getTypesAnnotatedWith(ClassMarker.class);

            Stream.concat(typesWithMarkedFields.stream(), markedTypes.stream())
                    .peek(clazz -> log.info("all marked types: {}", clazz.getName()))
                    .distinct()
                    .peek(clazz -> log.info("distinct marked types: {}", clazz.getName()))
                    .forEach(clazz -> {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            try {
                                FieldMarker fieldMarker = fields[i].getDeclaredAnnotation(FieldMarker.class);
                                setAttributeValue(fieldMarker, FieldMarker.class, "position", i + 1);
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
            Reflections reflections =
                    new Reflections(new FieldAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
            List<Class<?>> typesWithMarkedFields = reflections.getFieldsAnnotatedWith(FieldMarker.class)
                    .stream()
                    .map(Field::getDeclaringClass)
                    .distinct()
                    .collect(Collectors.toList());

            Set<Class<?>> markedTypes = reflections.getTypesAnnotatedWith(ClassMarker.class);

            Stream.concat(typesWithMarkedFields.stream(), markedTypes.stream())
                    .peek(clazz -> log.info("all marked types: {}", clazz.getName()))
                    .distinct()
                    .peek(clazz -> log.info("distinct marked types: {}", clazz.getName()))
                    .forEach(clazz -> {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            try {
                                FieldMarker fieldMarker = fields[i].getDeclaredAnnotation(FieldMarker.class);
                                int position = i + 1;

                                FieldMarker overridingFieldMarker = fieldMarker == null ? createFieldMarkerAnnotation(
                                        position) : createFieldMarkerAnnotation(fieldMarker, position);
                                Method method = fields[i].getClass().getDeclaredMethod("declaredAnnotations");
                                method.setAccessible(true);

                                Map<Class<? extends Annotation>, Annotation> annotationsMap =
                                        (Map<Class<? extends Annotation>, Annotation>) method.invoke(fields[i]);

                                if (annotationsMap.isEmpty()) {
                                    annotationsMap = new HashMap<>();
                                    Field root = (Field) ROOT_FIELD.get(fields[i]);
                                    DECLARED_ANNOTATIONS.set(fields[i], annotationsMap);
                                    DECLARED_ANNOTATIONS.set(root, annotationsMap);

                                    // test with Java 8:
                                    // Field declaredAnnotations = Field.class.getDeclaredField("declaredAnnotations");
                                    // declaredAnnotations.set(fields[i], annotationsMap);
                                    annotationsMap.put(FieldMarker.class, overridingFieldMarker);


                                    //                                    Map<Class<? extends Annotation>,
                                    //                                    Annotation> test =
                                    //                                            (Map<Class<? extends Annotation>,
                                    //                                            Annotation>) method.invoke(fields[i]);
                                    //
                                    //                                    int redefinedCount = (int) REDEFINED_COUNT
                                    //                                    .get(fields[i].getClass());
                                    //                                    int rootRedefinedCount = (int)
                                    //                                    REDEFINED_COUNT.get(root.getClass());
                                    //
                                    //                                    ANNOTATION_DATA.set(fields[i].getClass(),
                                    //                                            ANNOTATION_DATA_CONSTRUCTOR
                                    //                                            .newInstance(annotationsMap,
                                    //                                            annotationsMap,
                                    //                                                    redefinedCount));
                                    //
                                    //                                    ANNOTATION_DATA.set(root.getClass(),
                                    //                                            ANNOTATION_DATA_CONSTRUCTOR
                                    //                                            .newInstance(annotationsMap,
                                    //                                            annotationsMap,
                                    //                                                    rootRedefinedCount));
                                    //
                                    //                                    addAnnotations(fields[i].getClass(),
                                    //                                    annotationsMap);
                                    //                                    addAnnotations(root.getClass(),
                                    //                                    annotationsMap);

                                    log.info("");
                                } else {
                                    annotationsMap.put(FieldMarker.class, overridingFieldMarker);

                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

        };
    }

    private FieldMarker createFieldMarkerAnnotation(int position) {
        return new FieldMarker() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FieldMarker.class;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public int position() {
                return position;
            }
        };
    }

    private FieldMarker createFieldMarkerAnnotation(FieldMarker fieldMarker, int position) {
        return new FieldMarker() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return FieldMarker.class;
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
        };
    }

    @Bean
    public ApplicationRunner validatingApplicationRunner() {
        return args -> {
            Field[] subModelFields = SubModel.class.getDeclaredFields();
            log.info("subModel:");
            for (Field field : subModelFields) {
                FieldMarker fieldMarker = field.getAnnotation(FieldMarker.class);
                log.info("{} - position = {}", field.getName(), fieldMarker.position());
            }

            log.info("-----");

            Field[] baseModelFields = BaseModel.class.getDeclaredFields();
            log.info("baseModel:");
            for (Field field : baseModelFields) {
                FieldMarker fieldMarker = field.getAnnotation(FieldMarker.class);
                log.info("{} - position = {}", field.getName(), fieldMarker.position());
            }

        };
    }

    public static Annotation setAttributeValue(Annotation annotation, Class<?> annotationType, String attributeName,
            Object newValue) {
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

    public static <T extends Annotation> void addAnnotations(Class<?> clazz,
            Map<Class<? extends Annotation>, Annotation> annotations) {
        try {
            while (true) {
                int classRedefinedCount = (int) REDEFINED_COUNT.get(clazz);
                Object annotationData = ANNOTATION_DATA_METHOD.invoke(clazz);

                Object newAnnotationData = changeClassAnnotationData(annotations, classRedefinedCount);

                if ((boolean) ATOMIC_CAS_ANNOTATION_DATA_METHOD.invoke(ATOMIC_CLASS, clazz, annotationData,
                        newAnnotationData)) {
                    break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object changeClassAnnotationData(Map<Class<? extends Annotation>, Annotation> annotations,
            int classRedefinedCount) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = new HashMap<>(annotations);
        return ANNOTATION_DATA_CONSTRUCTOR.newInstance(annotations, declaredAnnotations, classRedefinedCount);
    }
}
