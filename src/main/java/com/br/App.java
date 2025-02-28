package com.br;

import com.br.annotations.Oblivion;
import com.br.annotations.OblivionPrototype;
import com.br.annotations.OblivionService;
import com.br.testingFiles.service.TaskService;
import com.br.testingFiles.service.UserService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class App {
  public static void main(String[] args) throws Exception {

    BeansContainer container = new BeansContainer();

    initializeSingletonBean("testUser", UserService.class, container);
    initializeSingletonBean("testUser", UserService.class, container);

    registerPrototypeBean("taskProto", TaskService.class, container);
    registerPrototypeBean("taskProto", TaskService.class, container);

    Object testUserObj = container.getSingletonBean("testUser");
    Object testUserObj2 = container.getSingletonBean("testUser");

    Object protoTaskServiceObj = container.getPrototypeBean("taskProto");
    Object protoTaskServiceObj2 = container.getPrototypeBean("taskProto");

    UserService testUser = UserService.class.cast(testUserObj);
    UserService testUser2 = UserService.class.cast(testUserObj2);

    System.out.println("TEST USER 1: " + testUser);
    System.out.println("TEST USER 2: " + testUser2);

    TaskService protoTaskService = TaskService.class.cast(protoTaskServiceObj);
    TaskService protoTaskService2 = TaskService.class.cast(protoTaskServiceObj2);

    System.out.println("PROTO TASK SERVICE 1: " + protoTaskService);
    System.out.println("PROTO TASK SERVICE 2: " + protoTaskService2);

    // NOTE: output will be something like:
    // TEST USER 1: com.br.testingFiles.service.UserService@41906a77
    // TEST USER 2: com.br.testingFiles.service.UserService@41906a77
    // PROTO TASK SERVICE 1: com.br.testingFiles.service.TaskService@4b9af9a9
    // PROTO TASK SERVICE 2: com.br.testingFiles.service.TaskService@5387f9e0
    // NOTE: same identifier for a singleton means same instance, but for a
    // prototype bean even if we are using the same identifier, we get
    // different instances
  }

  public static class BeansContainer {
    private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();
    private Class<?>[] requiredParams;
    private Object[] requiredObjects;

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
            initializeFields(initPrototypeBean);
            return initPrototypeBean;
          } else {
            Object initPrototypeBean =
                prototypeBeanClass
                    .getDeclaredConstructor(requiredParams)
                    .newInstance(requiredObjects);
            initializeFields(initPrototypeBean);
            return initPrototypeBean;
          }
        }
      }

      return null;
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
  public static void initializeFields(Object object) throws Exception {
    Class<?> clazz = object.getClass();
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
          field.set(object, 0);
        }

        if (field.getType().isAssignableFrom(boxedIntegerType)) {
          field.set(object, 0);
        }

        if (field.getType().isAssignableFrom(stringType)) {
          field.set(object, "Default");
        }

        if (field.getType().isAssignableFrom(arrayListType)) {
          field.set(object, new ArrayList<>());
        }
      }
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
          initializeFields(container.getSingletonBean(identifier));
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
            initializeFields(container.getSingletonBean(paramName));
            requiredParams.add(paramType);
            requiredObjects.add(container.getSingletonBean(paramName));
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          T initClass =
              clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
          container.registerSingletonBean(identifier, initClass);
          initializeFields(container.getSingletonBean(identifier));
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
            initializeFields(container.getSingletonBean(customDependencyName));
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
