package com.br;

import com.br.annotations.Oblivion;
import com.br.annotations.OblivionPostConstruct;
import com.br.annotations.OblivionPreDestroy;
import com.br.annotations.OblivionPrototype;
import com.br.annotations.OblivionService;
import com.br.testingFiles.service.UserService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class App {
  public static class Shutdown {
    public void attachShutdown(BeansContainer container) {
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread() {
                @Override
                public void run() {
                  for (Map.Entry<Object, Method> entry : container.preDestroyMethods.entrySet()) {
                    try {
                      runPreDestroyMethods(entry.getKey(), entry.getValue());
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                  container.preDestroyMethods.clear();
                  container.singletonBeans.clear();
                  container.prototypeBeans.clear();
                }
              });
    }
  }

  public static void main(String[] args) throws Exception {
    Shutdown shutdownHook = new Shutdown();
    BeansContainer container = new BeansContainer();
    shutdownHook.attachShutdown(container);
    try {
      initializeSingletonBean("testUser", UserService.class, container);
      Object testUserObj = container.getSingletonBean("testUser");
      UserService testUser = UserService.class.cast(testUserObj);
      System.out.println("TEST USER 1: " + testUser);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public static class BeansContainer {
    private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();
    // methods annotated with @PreDestroy
    private Map<Object, Method> preDestroyMethods = new ConcurrentHashMap<>();

    public <T> void registerSingletonBean(String identifier, T bean) {
      singletonBeans.put(identifier, bean);
    }

    public <T> void registerPrototypeBean(
        String identifier, PrototypeBeanMetadata prototypeBeanMetadata) {
      prototypeBeans.put(identifier, prototypeBeanMetadata);
    }

    public Object getSingletonBean(String identifier) {
      Object singletonBean = singletonBeans.get(identifier);
      if (singletonBean != null) {
        return singletonBean;
      }
      return null;
    }

    public Object getPrototypeBean(String identifier) throws Exception {

      Class<?> prototypeBeanClass = prototypeBeans.get(identifier).prototypeClass;
      Class<?>[] requiredParams = prototypeBeans.get(identifier).requiredParams;
      Object[] requiredObjects = prototypeBeans.get(identifier).requiredObjects;

      if (prototypeBeanClass != null) {

        Constructor<?>[] ctors = prototypeBeanClass.getDeclaredConstructors();

        for (Constructor<?> ctor : ctors) {
          if (ctor.getParameterCount() == 0) {
            Object initPrototypeBean = prototypeBeanClass.newInstance();
            initializeFieldsAndMethods(initPrototypeBean, null);
            return initPrototypeBean;
          } else {
            Object initPrototypeBean =
                prototypeBeanClass
                    .getDeclaredConstructor(requiredParams)
                    .newInstance(requiredObjects);
            initializeFieldsAndMethods(initPrototypeBean, null);
            return initPrototypeBean;
          }
        }
      }

      return null;
    }

    public void registerPreDestroyMethods(Object instantiatedClass, Method method) {
      preDestroyMethods.put(instantiatedClass, method);
    }

    public Map<?, Object> getAllSingletonBeans() {
      if (singletonBeans.isEmpty()) {
        return null;
      }
      return singletonBeans;
    }
  }

  public static class PrototypeBeanMetadata {
    private Class<?> prototypeClass;
    private Class<?>[] requiredParams;
    private Object[] requiredObjects;

    public PrototypeBeanMetadata() {}

    public PrototypeBeanMetadata(
        Class<?> prototypeClass, Class<?>[] requiredParams, Object[] requiredObjects) {
      this.prototypeClass = prototypeClass;
      this.requiredParams = requiredParams;
      this.requiredObjects = requiredObjects;
    }

    public Class<?> getPrototypeClass() {
      return prototypeClass;
    }

    public void setPrototypeClass(Class<?> prototypeClass) {
      this.prototypeClass = prototypeClass;
    }

    public Class<?>[] getRequiredParams() {
      return requiredParams;
    }

    public void setRequiredParams(Class<?>[] requiredParams) {
      this.requiredParams = requiredParams;
    }

    public Object[] getRequiredObjects() {
      return requiredObjects;
    }

    public void setRequiredObjects(Object[] requiredObjects) {
      this.requiredObjects = requiredObjects;
    }
  }

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

      if (method.isAnnotationPresent(OblivionPreDestroy.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          container.registerPreDestroyMethods(instantiatedClass, method);
        }
      }
    }
  }

  // NOTE: objectToRun here is the class where the method is
  public static void runPreDestroyMethods(Object objectToRun, Method methodToRun) throws Exception {
    if (objectToRun != null && objectToRun != null) {
      methodToRun.invoke(objectToRun);
    }
  }

  // TODO: gotta add suport for instantiating multiple constructors here too
  public static <T> void initializeSingletonBean(
      String identifier, Class<T> clazz, BeansContainer container) throws Exception {
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();

      for (Constructor<?> ctor : ctors) {
        if (ctor.getParameterCount() == 0) {
          // NOTE: newInstance is deprecated, gotta see other way to do it
          T init = clazz.newInstance();
          container.registerSingletonBean(identifier, init);
          initializeFieldsAndMethods(container.getSingletonBean(identifier), container);
        } else {
          Parameter[] params = ctor.getParameters();

          // required params to use inside getDeclaredConstructor
          List<Class<?>> requiredParams = new ArrayList<>();
          // required objects to use inside newInstance
          List<Object> requiredObjects = new ArrayList<>();

          for (Parameter p : params) {
            Class<?> paramType = p.getType();
            String paramName = p.getType().getName();
            Object initParam = paramType.newInstance();
            container.registerSingletonBean(paramName, initParam);
            initializeFieldsAndMethods(container.getSingletonBean(paramName), container);
            requiredParams.add(paramType);
            requiredObjects.add(container.getSingletonBean(paramName));
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          T initClass =
              clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
          container.registerSingletonBean(identifier, initClass);
          initializeFieldsAndMethods(container.getSingletonBean(identifier), container);
        }
      }
    }
  }

  // TODO: it needs improvement like when we have nested annotations, but i can do that later
  public static <T> void registerPrototypeBean(
      String identifier, Class<T> clazz, BeansContainer container) throws Exception {
    if (clazz.isAnnotationPresent(OblivionPrototype.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();

      PrototypeBeanMetadata prototypeBeanMetadata = new PrototypeBeanMetadata();

      for (Constructor<?> ctor : ctors) {
        if (ctor.getParameterCount() == 0) {
          prototypeBeanMetadata.setPrototypeClass(clazz);
          prototypeBeanMetadata.setRequiredParams(null);
          prototypeBeanMetadata.setRequiredObjects(null);
          container.registerPrototypeBean(identifier, prototypeBeanMetadata);
        } else {
          Parameter[] params = ctor.getParameters();

          // required params to use inside getDeclaredConstructor
          List<Class<?>> requiredParams = new ArrayList<>();
          // required objects to use inside newInstance
          List<Object> requiredObjects = new ArrayList<>();

          for (Parameter p : params) {
            Class<?> paramType = p.getType();
            String paramName = p.getType().getName();
            Object initParam = paramType.newInstance();
            // even though in this method we are registering prototype beans,
            // a dependency of a prototype bean is still a singleton
            String customDependencyName =
                LocalDateTime.now() + identifier + paramName + clazz.getName();
            container.registerSingletonBean(customDependencyName, initParam);
            initializeFieldsAndMethods(container.getSingletonBean(customDependencyName), container);
            requiredParams.add(paramType);
            requiredObjects.add(container.getSingletonBean(customDependencyName));
            prototypeBeanMetadata.setPrototypeClass(clazz);
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          prototypeBeanMetadata.setRequiredParams(requiredParamsArr);
          prototypeBeanMetadata.setRequiredObjects(requiredObjectsArr);

          container.registerPrototypeBean(identifier, prototypeBeanMetadata);
        }
      }
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
