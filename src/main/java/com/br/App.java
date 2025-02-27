package com.br;

import com.br.model.User;
import com.br.service.TaskService;
import com.br.service.UserService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class App {
  public static void main(String[] args) throws Exception {

    BeansContainer container = new BeansContainer();

    // here initializeClass is working like Spring's @Autowired
    initializeClass("userService", UserService.class, container);
    initializeClass("userService2", UserService.class, container);
    initializeClass("taskService", TaskService.class, container);

    for (Map.Entry<?, Object> bean : container.beans.entrySet()) {
      //   checkIfAnnotated(bean.getValue());
      //   initializeFields(bean.getValue());
      //   logAnnotatedFields(bean.getValue());
      // logAnnotatedClasses(bean.getValue());
      System.out.println("printing: " + bean.getValue());
    }

    // i dont know if this is the correct way to do this shit,
    // seems like too much stuff to do... im trying to add
    // 'beans identifier', so we can refer to a bean using
    // different names, so we can use in different scenarios,
    // but it means that now i have to return an object from
    // the beans container, and when i want to use it, i need
    // to cast the object to the type i want, like i do below
    //
    // it works, but idk if its the correct way to do it, might
    // have to look into that later...
    Object userServiceObj = container.getBean("userService");
    Object userServiceObj2 = container.getBean("userService2");
    Object taskServiceObj = container.getBean("taskService");

    UserService userServiceCaller = UserService.class.cast(userServiceObj);
    UserService userServiceCaller2 = UserService.class.cast(userServiceObj2);
    TaskService taskServiceCaller = TaskService.class.cast(taskServiceObj);

    User userOne = new User("Renan", 24);
    User userTwo = new User("naneR", 42);

    // here im using addUser from UserService without manually instantiating it
    userServiceCaller.addUser(userOne);
    userServiceCaller.addUser(userTwo);

    // here im testing this TaskService class, it depends on UserService to work,
    // so if it works (it does lol), it means that i implemented constructor
    // injection too
    taskServiceCaller.assignTaskToUser("code", userOne);

    System.out.println("checking added user: " + userServiceCaller.getUsers().toString());
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Oblivion {
    public String key() default "";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface OblivionService {
    public String key() default "";
  }

  public static class BeansContainer {
    private final Map<String, Object> beans = new ConcurrentHashMap<>();

    // NOTE: here i made putBean kinda loose typed, so it allows us to
    // store anything, because i was having problems to store classes
    // from inside constructors.
    // i tried casting the type before adding to the Map here, at the
    // 'else' statement in the 'initializeClass', but it wasn't working.
    // to still have some type-safety, im keeping type restriction on
    // getBean
    public <T> void putBean(String identifier, T bean) {
      beans.put(identifier, bean);
    }

    public Object getBean(String identifier) {
      Object bean = beans.get(identifier);
      if (bean != null) {
        return bean;
      }
      return null;
    }

    public Map<?, Object> getAllBeans() {
      if (beans.isEmpty()) {
        return null;
      }
      return beans;
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
        System.out.println("Found field with OBLIVION: " + field.getName());

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
  public static <T> void initializeClass(
      String identifier, Class<T> clazz, BeansContainer container) throws Exception {
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();

      for (Constructor<?> ctor : ctors) {
        if (ctor.getParameterCount() == 0) {
          // NOTE: newInstance is deprecated, gotta see other way to do it
          T init = clazz.newInstance();
          container.putBean(identifier, init);
          initializeFields(container.getBean(identifier));
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
            container.putBean(paramName, initParam);
            initializeFields(container.getBean(paramName));
            requiredParams.add(paramType);
            requiredObjects.add(container.getBean(paramName));
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          T initClass =
              clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
          container.putBean(identifier, initClass);
          initializeFields(container.getBean(identifier));
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
