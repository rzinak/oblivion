package com.br;

import com.br.model.User;
import com.br.service.UserService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class App {
  public static void main(String[] args) throws Exception {

    BeansContainer container = new BeansContainer();

    // here initializeClass is working like Spring's @Autowired
    initializeClass(UserService.class, container);
    initializeFields(container.getBean(UserService.class));

    for (Map.Entry<?, Object> bean : container.beans.entrySet()) {
      //   // checkIfAnnotated(bean.getValue());
      //   initializeFields(bean.getValue());
      //   logAnnotatedFields(bean.getValue());
      logAnnotatedClasses(bean.getValue());
    }

    UserService caller = container.getBean(UserService.class);

    User newUser = new User("Renan", 24);

    // here im using addUser from UserService without manually instantiating it
    caller.addUser(newUser);

    System.out.println("checking added user: " + caller.getUsers().toString());
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
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    public <T> void putBean(Class<T> type, T bean) {
      beans.put(type, bean);
    }

    public <T> T getBean(Class<T> type) {
      Object bean = beans.get(type);
      if (bean != null && type.isInstance(bean)) {
        return type.cast(bean);
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

  public static <T> void initializeClass(Class<T> clazz, BeansContainer container)
      throws Exception {
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      T init = clazz.newInstance(); // NOTE: newInstance is deprecated, gotta see other way to do it
      container.putBean(clazz, init);
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
