package com.br;

import com.br.service.ColorService;
import com.br.service.CountryService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// NOTE: final goal -> be able to use a class without manually instantiating it
public class App {
  public static void main(String[] args) throws Exception {
    System.out.println("Hello World!");

    BeansContainer container = new BeansContainer();

    container.putBean(ColorService.class, new ColorService());
    container.putBean(CountryService.class, new CountryService());

    for (Map.Entry<?, Object> bean : container.beans.entrySet()) {
      // System.out.println(bean.getKey() + "/" + bean.getValue());
      // checkIfAnnotated(bean.getValue());
      System.out.println("loggin values before:");
      logAnnotatedFields(bean.getValue());
      initializeFields(bean.getValue());
      System.out.println("loggin values after:");
      logAnnotatedFields(bean.getValue());
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Oblivion {
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

  public static void initializeFields(Object object) throws Exception {
    Class<?> clazz = object.getClass();
    Class<?> integerType = int.class;
    Class<?> boxedIntegerType = Integer.class;
    Class<?> stringType = String.class;
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(Oblivion.class)) {
        System.out.println("Found field with OBLIVION: " + field.getName());

        if (field.getType().isAssignableFrom(stringType)) {
          field.set(object, "Default");
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
        System.out.println("field: " + field.getName() + " has value: " + field.get(object));
      }
    }
  }

  // from here below they all code i copied from the internet as a
  // reference for a custom annotation implementation

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface JsonSerializable {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface JsonElement {
    public String key() default "";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Init {} // its to instantiate stuff

  @JsonSerializable
  public static class Person {

    @JsonElement private String firstName;

    @JsonElement private String lastName;

    @JsonElement(key = "personAge")
    private String age;

    private String address;

    public Person() {}

    public Person(String firstName, String lastName, String age, String address) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.age = age;
      this.address = address;
    }

    @Init
    private void initNames() {
      this.firstName = this.firstName.substring(0, 1).toUpperCase() + this.firstName.substring(1);
      this.lastName = this.lastName.substring(0, 1).toUpperCase() + this.lastName.substring(1);
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getAge() {
      return age;
    }

    public void setAge(String age) {
      this.age = age;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }
  }

  private static void checkIfSerializable(Object object) throws Exception {
    if (Objects.isNull(object)) {
      throw new Exception("The object to serialize is null");
    }

    Class<?> clazz = object.getClass();
    if (!clazz.isAnnotationPresent(JsonSerializable.class)) {
      throw new Exception(
          "The class " + clazz.getSimpleName() + " is not annotated with JsonSerializable");
    }
  }

  private static void initializeObject(Object object) throws Exception {
    Class<?> clazz = object.getClass();
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Init.class)) {
        method.setAccessible(true);
        method.invoke(object);
      }
    }
  }

  private static String getJsonString(Object object) throws Exception {
    Class<?> clazz = object.getClass();
    Map<String, String> jsonElementsMap = new HashMap<>();
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(JsonElement.class)) {
        jsonElementsMap.put(getKey(field), (String) field.get(object));
      }
    }

    String jsonString =
        jsonElementsMap.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
            .collect(Collectors.joining(","));
    return "{" + jsonString + "}";
  }

  private static String getKey(Field field) {
    String value = field.getAnnotation(JsonElement.class).key();
    return value.isEmpty() ? field.getName() : value;
  }

  public static class ObjectToJsonConverter {
    public String convertToJson(Object object) throws Exception {
      try {
        checkIfSerializable(object);
        initializeObject(object);
        return getJsonString(object);
      } catch (Exception ex) {
        throw new Exception(ex.getMessage());
      }
    }
  }
}
