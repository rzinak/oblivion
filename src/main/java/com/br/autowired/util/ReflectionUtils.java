package com.br.autowired.util;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreInitialization;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.annotations.OblivionService;
import com.br.autowired.container.BeansContainer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ReflectionUtils {
  public static void checkIfAnnotated(Object object) throws Exception {
    if (Objects.isNull(object)) {
      throw new Exception("The object is null");
    }

    System.out.println("Annotation is present for class: " + object.getClass());
    Class<?> retClass = object.getClass();

    if (!retClass.isAnnotationPresent(Oblivion.class)) {
      throw new Exception("Oblivion annotation is not present");
    }
  }

  // TODO: ADD SUPPORT FOR MORE TYPES HERE
  // NOTE: as this method grows, it makes sense to split this into different methods, for example,
  // different methods to inject primitives, non-primitive fields, etc...
  public static void initializeFieldsAndMethods(Object instantiatedClass, BeansContainer container)
      throws Exception {
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
      System.out.println("field name: " + field.getName());
      if (field.isAnnotationPresent(Oblivion.class)) {
        if (field.getType().isAssignableFrom(integerType)) {
          field.set(instantiatedClass, 0);
        }

        if (field.getType().isAssignableFrom(boxedIntegerType)) {
          field.set(instantiatedClass, 0);
        }

        if (field.getType().isAssignableFrom(stringType)) {
          field.set(instantiatedClass, "Default");
        }

        if (field.getType().isAssignableFrom(arrayListType)) {
          field.set(instantiatedClass, new ArrayList<>());
        }
      }
    }

    // NOTE: because by convention post construct invocations occur only
    // on void methods and with no args, im doing the same here
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(OblivionPostConstruct.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          method.invoke(instantiatedClass);
        }
      }

      if (method.isAnnotationPresent(OblivionPreInitialization.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          method.invoke(instantiatedClass);
        }
      }

      if (method.isAnnotationPresent(OblivionPreDestroy.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPreDestroyMethods(instantiatedClass, method);
        }
      }

      if (method.isAnnotationPresent(OblivionPreShutdown.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPreShutdownMethods(instantiatedClass, method);
        }
      }
    }
  }

  public static void runPreInitializationMethods(Method[] methods, Object objectToRun)
      throws Exception {
    if (objectToRun != null && methods != null) {
      Stream.of(methods)
          .filter(m -> m.isAnnotationPresent(OblivionPreInitialization.class))
          .filter(m -> m.getParameters().length == 0)
          .filter(m -> m.getReturnType().equals(Void.TYPE))
          .forEach(
              m -> {
                try {
                  m.invoke(objectToRun);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              });
    }
  }

  // NOTE: objectToRun here is the class where the method is
  public static void runPreDestroyMethod(Object objectToRun, Method methodToRun) throws Exception {
    if (objectToRun != null && objectToRun != null) {
      methodToRun.invoke(objectToRun);
    }
  }

  public static void runPreShutdownMethod(Object objectToRun, Method methodToRun) throws Exception {
    if (objectToRun != null && objectToRun != null) {
      methodToRun.invoke(objectToRun);
    }
  }

  public static String getElementsKey(Field field) {
    String value = field.getAnnotation(Oblivion.class).key();
    return value.isEmpty() ? field.getName() : value;
  }

  public static void logAnnotatedFields(Object object) throws Exception {
    Class<?> clazz = object.getClass();
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(Oblivion.class)) {
        System.out.println("Field: " + field.getName() + " has value: " + field.get(object));
      }
    }
  }

  public static void logAnnotatedClasses(Object object) throws Exception {
    Class<?> clazz = object.getClass();
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      System.out.println("Class: " + clazz.getName() + " has value: " + clazz.getDeclaredClasses());
    }
  }
}
