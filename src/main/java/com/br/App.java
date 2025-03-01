// @OblivionPostConstruct -> marks a method to be executed after the bean is fully constructed and
//
// dependencies are injected basically mimicking spring's init(), methods annotated with it are
// by convention methods with no args and returns nothing - void.
// implementation of this doesnt seem hard, just need to check of methods with this annotation
// after registering a new bean, and just invoke the method right away.

// @OblivionPreDestroy -> marks a method to be executed before the bean is destroyed

// for this one the approach will be a little different, i gotta track beans with this annotation,
// so most likely ima keep a list of them.
// and when the application is or container is shutting down, i must iterate through the list and
// invoke the methods annotated with @OblivionPreDestroy.
// rn i aint have no container shutdown thing... there are 2 ways of doing it, one is a manual
// cleanup, and the other i believe must have be related to a shutdown hook... and im def going for
// the latter, cuz it seems more difficult lol. but i need to understand shutdown hooks more in
// depth before doing it

package com.br;

import com.br.annotations.Oblivion;
import com.br.annotations.OblivionPostConstruct;
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
  public static void main(String[] args) throws Exception {

    BeansContainer container = new BeansContainer();

    initializeSingletonBean("testUser", UserService.class, container);
    Object testUserObj = container.getSingletonBean("testUser");
    UserService testUser = UserService.class.cast(testUserObj);
    System.out.println("TEST USER 1: " + testUser);
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
      System.out.println("field name: " + field.getName());
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

    // NOTE: because by convention post construct invocations occur only
    // on void methods and with no args, im doing the same here
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(OblivionPostConstruct.class)) {
        if (method.getParameters().length == 0 && method.getReturnType().equals(Void.TYPE)) {
          method.invoke(object);
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
