package com.br.autowired.util;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
import com.br.autowired.annotations.OblivionPostInitialization;
import com.br.autowired.annotations.OblivionPostShutdown;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreInitialization;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.annotations.OblivionService;
import com.br.autowired.container.BeansContainer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
  }

  // NOTE: registers @OblivionPreDestroy, @OblivionPreShutdown, @OblivionPostShutdown
  public static void registerPersistentBeanLifecycles(
      Class<?> clazz, Object objectToRun, BeansContainer container) throws Exception {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(OblivionPreDestroy.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPreDestroyMethods(objectToRun, method);
        }
      }

      if (method.isAnnotationPresent(OblivionPreShutdown.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPreShutdownMethods(objectToRun, method);
        }
      }

      if (method.isAnnotationPresent(OblivionPostShutdown.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPostShutdownMethods(objectToRun, method);
        }
      }
    }
  }

  // NOTE: @OblivionPreInitialization should be a static method
  public static void runPreInitializationMethods(Class<?> clazz) throws Exception {
    if (clazz != null) {
      Method[] methods = clazz.getDeclaredMethods();
      Stream.of(methods)
          .filter(m -> Modifier.isStatic(m.getModifiers()))
          .filter(m -> m.isAnnotationPresent(OblivionPreInitialization.class))
          .filter(m -> m.getParameters().length == 0)
          .filter(m -> m.getReturnType().equals(Void.TYPE))
          .forEach(
              m -> {
                try {
                  // since its static, we pass null because static methods
                  // belong to the class itself and not to any particular instance
                  m.invoke(null);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              });
    }
  }

  public static void runPostConstrucMethods(Class<?> clazz, Object objectToRun) throws Exception {
    if (clazz != null) {
      Method[] methods = clazz.getDeclaredMethods();
      Stream.of(methods)
          .filter(m -> m.isAnnotationPresent(OblivionPostConstruct.class))
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

  public static void runPostInitializaionMethods(Class<?> clazz, Object objectToRun)
      throws Exception {
    if (clazz != null) {
      Method[] methods = clazz.getDeclaredMethods();
      Stream.of(methods)
          .filter(m -> m.isAnnotationPresent(OblivionPostInitialization.class))
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

  public static void runPostShutdownMethod(Object objectToRun, Method methodToRun)
      throws Exception {
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
