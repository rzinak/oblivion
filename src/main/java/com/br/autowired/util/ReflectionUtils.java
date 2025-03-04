package com.br.autowired.util;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
import com.br.autowired.annotations.OblivionPostInitialization;
import com.br.autowired.annotations.OblivionPostShutdown;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreInitialization;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.exception.OblivionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

  // NOTE: @OblivionPreInitialization should be a static method
  public static void runPreInitializationMethods(Class<?> clazz) throws OblivionException {
    if (clazz == null) {
      throw new IllegalArgumentException("Class reference cannot be null");
    }

    Method[] methods = clazz.getDeclaredMethods();
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

    Method[] methods = clazz.getDeclaredMethods();

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

    Method[] methods = clazz.getDeclaredMethods();

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

  // NOTE: objectToRun here is the class where the method is
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
