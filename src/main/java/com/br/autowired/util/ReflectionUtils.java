package com.br.autowired.util;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
import com.br.autowired.annotations.OblivionPostInitialization;
import com.br.autowired.annotations.OblivionPostShutdown;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreInitialization;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.container.Pair;
import com.br.autowired.exception.OblivionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {
  // TODO: ADD SUPPORT FOR MORE TYPES HERE
  // TODO: also break this into different functions when adding more types
  // like initializePrimitives, initializeNonPrimitives... maybe use helper
  // function in case there are too many types
  public static void initializeFields(Object instantiatedClass) throws Exception {
    Class<?> clazz = instantiatedClass.getClass();
    Class<?> integerType = int.class;
    Class<?> boxedIntegerType = Integer.class;
    Class<?> stringType = String.class;
    Class<?> arrayListType = ArrayList.class;
    Class<?> listType = List.class;
    Class<?> mapType = Map.class;
    Class<?> objectType = Object.class;

    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(Oblivion.class)) {
        if (field.getType().isAssignableFrom(integerType)) {
          try {
            field.set(instantiatedClass, 0);
          } catch (IllegalAccessException
              | IllegalArgumentException
              | NullPointerException
              | ExceptionInInitializerError ex) {
            throw new OblivionException(
                String.format(
                    "Error setting field '%s' in class '%s': %s",
                    field.getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }

        if (field.getType().isAssignableFrom(boxedIntegerType)) {
          try {
            field.set(instantiatedClass, 0);
          } catch (IllegalAccessException
              | IllegalArgumentException
              | NullPointerException
              | ExceptionInInitializerError ex) {
            throw new OblivionException(
                String.format(
                    "Error setting field '%s' in class '%s': %s",
                    field.getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }

        if (field.getType().isAssignableFrom(stringType)) {
          try {
            field.set(instantiatedClass, "Default");
          } catch (IllegalAccessException
              | IllegalArgumentException
              | NullPointerException
              | ExceptionInInitializerError ex) {
            throw new OblivionException(
                String.format(
                    "Error setting field '%s' in class '%s': %s",
                    field.getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }

        if (field.getType().isAssignableFrom(arrayListType)) {
          try {
            field.set(instantiatedClass, new ArrayList<>());
          } catch (IllegalAccessException
              | IllegalArgumentException
              | NullPointerException
              | ExceptionInInitializerError ex) {
            throw new OblivionException(
                String.format(
                    "Error setting field '%s' in class '%s': %s",
                    field.getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }
      }
    }
  }

  // NOTE: registers @OblivionPreDestroy, @OblivionPreShutdown, @OblivionPostShutdown
  public static void registerPersistentBeanLifecycles(
      Class<?> clazz, Object objectToRun, BeansContainer container) throws OblivionException {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(OblivionPreDestroy.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          try {
            container.registerPreDestroyMethods(objectToRun, method);
          } catch (Exception ex) {
            throw new OblivionException(
                String.format(
                    "Error when registering pre-destroy method '%s' in class '%s': %s",
                    method.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
          }
        }
      }

      if (method.isAnnotationPresent(OblivionPreShutdown.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          try {
            container.registerPreShutdownMethods(objectToRun, method);
          } catch (Exception ex) {
            throw new OblivionException(
                String.format(
                    "Error when registering pre-shutdown method '%s' in class '%s': %s",
                    method.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
          }
        }
      }

      if (method.isAnnotationPresent(OblivionPostShutdown.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          try {
            container.registerPostShutdownMethods(objectToRun, method);
          } catch (Exception ex) {
            throw new OblivionException(
                String.format(
                    "Error when registering post-shutdown method '%s' in class '%s': %s",
                    method.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
          }
        }
      }
    }
  }

  private static List<String> validateMethods(
      Method[] methods, Class<?> clazz, Class<?> annotationClass) {
    List<String> validationErrors = new ArrayList<>();

    for (Method m : methods) {
      if (m.isAnnotationPresent((Class<? extends Annotation>) annotationClass)) {
        if (annotationClass.equals(OblivionPreInitialization.class)
            && !Modifier.isStatic(m.getModifiers())) {
          validationErrors.add(
              String.format(
                  "@%s error -> Method: '%s' in class '%s' must be a static method!",
                  annotationClass.getSimpleName(), m.getName(), clazz.getSimpleName()));
        }
        if (m.getParameters().length != 0) {
          validationErrors.add(
              String.format(
                  "@%s error -> Method: '%s' in class '%s' must have no parameters!",
                  annotationClass.getSimpleName(), m.getName(), clazz.getSimpleName()));
        }

        if (!m.getReturnType().equals(Void.TYPE)) {
          validationErrors.add(
              String.format(
                  "@%s error -> Method: '%s' in class '%s' must return void!",
                  annotationClass.getSimpleName(), m.getName(), clazz.getSimpleName()));
        }
      }
    }
    return validationErrors;
  }

  private static List<String> executeAnnotatedMethods(
      Method[] methods, Object objectToRun, Class<?> annotationClass) {

    List<String> executionErrors = new ArrayList<>();

    for (Method m : methods) {
      if (m.isAnnotationPresent((Class<? extends Annotation>) annotationClass)) {
        try {
          m.invoke(objectToRun);
        } catch (Exception ex) {
          executionErrors.add(
              String.format(
                  "Failed to invoke @%s method '%s' in class '%s'. Reason: %s",
                  annotationClass.getSimpleName(),
                  m.getName(),
                  objectToRun.getClass().getSimpleName(),
                  ex.getMessage()));
        }
      }
    }

    return executionErrors;
  }

  public static List<String> validateMethodOrder(
      Method[] methods, Class<?> clazz, Class<? extends Annotation> annotationClass)
      throws OblivionException {
    List<String> orderValidationErrors = new ArrayList<>();

    for (Method m : methods) {
      Annotation annotation = m.getAnnotation(annotationClass);
      if (annotation != null) {
        try {
          // NOTE: this orderMethod is not the method itself, but the property "order"
          // that we can use in the annotation
          Method orderMethod = annotationClass.getMethod("order");
          int order = (Integer) orderMethod.invoke(annotation);
          if (order < 0) {
            orderValidationErrors.add(
                String.format(
                    "@%s error -> 'order' property in method: '%s' inside class '%s' should be 0 or"
                        + " any value greater than 0.\n"
                        + "HINT: You can also leave it empty, default value will be 0.",
                    annotationClass.getSimpleName(), m.getName(), clazz.getSimpleName()));
          }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
          throw new OblivionException(
              "Error while validating method order of execution: " + ex.getMessage());
        }
      }
    }
    return orderValidationErrors;
  }

  public static <T extends Annotation> void validateAndSortMethods(
      List<Pair<Object, Method>> methods, Class<T> annotationClass) throws OblivionException {
    Method[] methodArray = methods.stream().map(Pair::getR).toArray(Method[]::new);
    List<String> validationErrors = validateMethodOrder(methodArray, Object.class, annotationClass);

    if (!validationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occurred during validation of @"
              + annotationClass.getSimpleName()
              + " methods:\n"
              + String.join("\n", validationErrors));
    }

    methods.sort(
        (a, b) -> {
          int orderA = getOrderFromAnnotation(a.getR(), annotationClass);
          int orderB = getOrderFromAnnotation(b.getR(), annotationClass);
          return Integer.compare(orderA, orderB);
        });
  }

  public static <T extends Annotation> int getOrderFromAnnotation(
      Method method, Class<T> annotationClass) {
    T annotation = method.getAnnotation(annotationClass);
    if (annotation == null) {
      throw new IllegalArgumentException(
          "Method is not annotated with @" + annotationClass.getSimpleName());
    }

    try {
      // NOTE: this orderMethod is not the method itself, but the property "order"
      // that we can use in the annotation
      Method orderMethod = annotationClass.getMethod("order");
      return (int) orderMethod.invoke(annotation);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new RuntimeException("Failed to extract 'order' value from annotation", ex);
    }
  }

  // NOTE: @OblivionPreInitialization should be a static method
  public static void runPreInitializationMethods(Class<?> clazz) throws OblivionException {
    if (clazz == null) {
      throw new IllegalArgumentException("Class reference cannot be null");
    }

    Method[] methods =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(OblivionPreInitialization.class))
            .toArray(Method[]::new);

    List<String> methodOrderValidationErrors =
        validateMethodOrder(methods, clazz, OblivionPreInitialization.class);

    if (!methodOrderValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPreInitialization ordering of methods:\n"
              + String.join("\n", methodOrderValidationErrors));
    }

    Arrays.sort(
        methods,
        (a, b) ->
            Integer.compare(
                a.getAnnotation(OblivionPreInitialization.class).order(),
                b.getAnnotation(OblivionPreInitialization.class).order()));

    List<String> methodValidationErrors =
        validateMethods(methods, clazz, OblivionPreInitialization.class);

    if (!methodValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPreInitialization methods:\n"
              + String.join("\n", methodValidationErrors));
    }

    List<String> executionErrors =
        executeAnnotatedMethods(methods, clazz, OblivionPreInitialization.class);

    if (!executionErrors.isEmpty()) {
      throw new OblivionException(
          "Errors occcured during pre-initialization:\n" + String.join("\n", executionErrors));
    }
  }

  public static void runPostConstructMethods(Class<?> clazz, Object objectToRun)
      throws OblivionException {
    if (clazz == null) {
      throw new IllegalArgumentException("Class reference cannot be null");
    }

    Method[] methods =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(OblivionPostConstruct.class))
            .toArray(Method[]::new);

    List<String> methodOrderValidationErrors =
        validateMethodOrder(methods, clazz, OblivionPostConstruct.class);

    if (!methodOrderValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPostConstruct ordering of methods:\n"
              + String.join("\n", methodOrderValidationErrors));
    }

    Arrays.sort(
        methods,
        (a, b) ->
            Integer.compare(
                a.getAnnotation(OblivionPostConstruct.class).order(),
                b.getAnnotation(OblivionPostConstruct.class).order()));

    List<String> methodValidationErrors =
        validateMethods(methods, clazz, OblivionPostConstruct.class);

    if (!methodValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPostConstruct methods:\n"
              + String.join("\n", methodValidationErrors));
    }

    List<String> executionErrors =
        executeAnnotatedMethods(methods, objectToRun, OblivionPostConstruct.class);

    if (!executionErrors.isEmpty()) {
      throw new OblivionException(
          "Errors occured during post-construction:\n" + String.join("\n", executionErrors));
    }
  }

  public static void runPostInitializationMethods(Class<?> clazz, Object objectToRun)
      throws OblivionException {
    if (clazz == null) {
      throw new IllegalArgumentException("Class referene cannot be null");
    }

    Method[] methods =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(OblivionPostInitialization.class))
            .toArray(Method[]::new);

    List<String> methodOrderValidationErrors =
        validateMethodOrder(methods, clazz, OblivionPostInitialization.class);

    if (!methodOrderValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPostInitialization ordering of methods:\n"
              + String.join("\n", methodOrderValidationErrors));
    }

    Arrays.sort(
        methods,
        (a, b) ->
            Integer.compare(
                a.getAnnotation(OblivionPostInitialization.class).order(),
                b.getAnnotation(OblivionPostInitialization.class).order()));

    List<String> methodValidationErrors =
        validateMethods(methods, clazz, OblivionPostInitialization.class);

    if (!methodValidationErrors.isEmpty()) {
      throw new OblivionException(
          "Error(s) occured during validation of @OblivionPostInitialization methods:\n"
              + String.join("\n", methodValidationErrors));
    }

    List<String> executionErrors =
        executeAnnotatedMethods(methods, objectToRun, OblivionPostInitialization.class);

    if (!executionErrors.isEmpty()) {
      throw new OblivionException(
          "Errors occured during post-initialization:\n" + String.join("\n", executionErrors));
    }
  }

  public static void runPreDestroyMethod(Object objectToRun, Method methodToRun)
      throws OblivionException {
    if (objectToRun == null || methodToRun == null) {
      throw new IllegalArgumentException("Object or Method reference cannot be nulll");
    }

    try {
      methodToRun.invoke(objectToRun);
    } catch (Exception ex) {
      throw new OblivionException(
          String.format(
              "Failed to invoke @OblivionPreShutdown method '%s' in class '%s'. Reason: %s",
              methodToRun.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
    }
  }

  public static void runPreShutdownMethod(Object objectToRun, Method methodToRun)
      throws OblivionException {
    if (objectToRun == null || methodToRun == null) {
      throw new IllegalArgumentException("Object or Method reference cannot be null");
    }

    try {
      methodToRun.invoke(objectToRun);
    } catch (Exception ex) {
      throw new OblivionException(
          String.format(
              "Failed to invoke @OblivionPreShutdown method '%s' in class '%s'. Reason: %s",
              methodToRun.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
    }
  }

  public static void runPostShutdownMethod(Object objectToRun, Method methodToRun)
      throws OblivionException {
    if (objectToRun == null || methodToRun == null) {
      throw new IllegalArgumentException("Object or Method reference cannot be null");
    }

    try {
      methodToRun.invoke(objectToRun);
    } catch (Exception ex) {
      throw new OblivionException(
          String.format(
              "Failed to invoke @OblivionPostShutdown method '%s' in class '%s'. Reason: %s",
              methodToRun.getName(), objectToRun.getClass().getSimpleName(), ex.getMessage()));
    }
  }
}
