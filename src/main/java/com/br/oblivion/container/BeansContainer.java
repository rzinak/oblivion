package com.br.oblivion.container;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class BeansContainer {
  private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
  private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();

  // lifecycle methods
  private List<Pair<Object, Method>> preDestroyMethods = new ArrayList<>();
  private List<Pair<Object, Method>> preShutdownMethods = new ArrayList<>();
  private List<Pair<Object, Method>> postShutdownMethods = new ArrayList<>();

  // classes loaded from oblivion.config
  public static Map<String, Class<?>> configBeans = new HashMap<>();

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

  public Object getPrototypeBean(
      String identifier, BeansContainer beansContainer, ThreadPoolExecutor threadPoolExecutor)
      throws Exception {
    try {
      // NOTE: im using allowCoreThreadsTimeOut because by default core threads never terminate,
      // even if they are idle, because as the name suggests, the pool mantains a "core" minimum
      // number of threads.
      //
      // NOTE: recreating threads can introduce some overhead, but since im designing this for
      // small applications, it shouldnt even be noticeable.
      //
      // NOTE: another thing is that if core threads are terminated when timing them out, new tasks
      // can have a small delay to run, since we gotta wait for new threads to be created, but
      // again, for smalls tasks this shouldnt be a problem.
      //
      // NOTE: the last thing is that if we add a time out to them, they are no longer "core"
      // in essence, but its more of a conceptual stuff, so i dont care
      threadPoolExecutor.allowCoreThreadTimeOut(true);
      Class<?> prototypeBeanClass = prototypeBeans.get(identifier).getPrototypeClass();
      Class<?>[] requiredParams = prototypeBeans.get(identifier).getRequiredParams();
      Object[] requiredObjects = prototypeBeans.get(identifier).getRequiredObjects();

      if (prototypeBeanClass != null) {
        Constructor<?>[] ctors = prototypeBeanClass.getDeclaredConstructors();

        for (Constructor<?> ctor : ctors) {
          if (ctor.getParameterCount() == 0) {
            Object initPrototypeBean = prototypeBeanClass.newInstance();
            ReflectionUtils.runPostConstructMethods(
                prototypeBeanClass, initPrototypeBean, threadPoolExecutor);
            ReflectionUtils.runPostInitializationMethods(
                prototypeBeanClass, initPrototypeBean, threadPoolExecutor);

            ReflectionUtils.registerPersistentBeanLifecycles(
                prototypeBeanClass, initPrototypeBean, beansContainer);
            return initPrototypeBean;
          } else {
            Object initPrototypeBean =
                prototypeBeanClass
                    .getDeclaredConstructor(requiredParams)
                    .newInstance(requiredObjects);
            ReflectionUtils.runPostConstructMethods(
                prototypeBeanClass, initPrototypeBean, threadPoolExecutor);
            ReflectionUtils.runPostInitializationMethods(
                prototypeBeanClass, initPrototypeBean, threadPoolExecutor);
            ReflectionUtils.registerPersistentBeanLifecycles(
                prototypeBeanClass, initPrototypeBean, beansContainer);
            return initPrototypeBean;
          }
        }
      }

      return null;
    } catch (NullPointerException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException ex) {
      throw new OblivionException("Error creating prototype bean: " + identifier, ex);
    }
  }

  // beans loaded from oblivion.config file
  public static void registerConfigBean(String identifier, Class<?> clazz) {
    configBeans.put(identifier, clazz);
  }

  public static Map<String, Class<?>> getConfigBeans() {
    return configBeans;
  }

  public void registerPreDestroyMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preDestroyMethod = new Pair<Object, Method>(instantiatedClass, method);
    preDestroyMethods.add(preDestroyMethod);
  }

  public void registerPreShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    preShutdownMethods.add(preShutdownMethod);
  }

  public void registerPostShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> postShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    postShutdownMethods.add(postShutdownMethod);
  }

  public Object resolveDependency(Class<?> clazzToResolve, String classIdentifier)
      throws Exception {
    // System.out.println("class identifier inside resolve dependency: " + classIdentifier);

    // check base case 1 - singleton already created
    Object existingInstance = getSingletonBean(clazzToResolve.getName());
    if (existingInstance != null) {
      // System.out.println("Found existing instance for: " + clazzToResolve.getSimpleName());
      return existingInstance;
    }

    // TODO: here i can handle circular deps
    // i can add the classToResolve to a "currentlyCreating" set, if its already present throw error

    // checking eligibility
    if (!clazzToResolve.isAnnotationPresent(OblivionService.class)) {
      throw new OblivionException(
          String.format(
              "Cannot resolve class '%s'! Is this class annotated with '@OblivionService'?",
              clazzToResolve.getSimpleName()));
    }

    // figure out clazz constructors
    Constructor<?> constructorToUse = findInjectableConstructor(clazzToResolve);

    // figure out clazz dependencies - direct dependencies
    Parameter[] parameters = constructorToUse.getParameters();

    // handle another base case - no dependencies or recursive step
    Object newlyCreatedInstance;

    if (parameters.length == 0) {
      // base case 2: no deps needed
      // System.out.println("no deps for: " + clazzToResolve.getSimpleName());
      newlyCreatedInstance = constructorToUse.newInstance();
    } else {
      // recursive step
      // System.out.println("need deps for " + clazzToResolve.getSimpleName());

      // here i store the resolved deps for this specific call
      List<Object> resolvedArguments = new ArrayList<>();

      // loop through params needed by clazzToResolve's constructor
      for (Parameter parameter : parameters) {
        Class<?> dependencyClass = parameter.getType();
        // System.out.println(
        //     "resolving dependency: "
        //         + dependencyClass.getSimpleName()
        //         + " for "
        //         + clazzToResolve.getSimpleName());

        // recursive call
        // here we gotta "ask" the container to get/create the dependency
        Object dependencyInstance = resolveDependency(dependencyClass, classIdentifier);

        resolvedArguments.add(dependencyInstance);
        // System.out.println("resolved dependency: " + dependencyClass.getSimpleName() + "
        // obtained");
      }

      // System.out.println(
      //     "all dependencies resolved for "
      //         + clazzToResolve.getSimpleName()
      //         + ", instantiating it....");
      newlyCreatedInstance = constructorToUse.newInstance(resolvedArguments.toArray());
    }

    // now gota register the new instance (we gotta do this before lifecycle methods that might need
    // injections)
    // System.out.println("registering singleton instance for: " + clazzToResolve.getSimpleName());
    registerSingletonBean(clazzToResolve.getSimpleName(), newlyCreatedInstance);

    // i think i can handle lifecycle steps here
    // like those runPostConstructMethods, initializeFields, etc...

    // also if i add that currentlyCreating set, here i think i should remove it

    return newlyCreatedInstance;
  }

  private Constructor<?> findInjectableConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Optional<Constructor<?>> constructorToInject =
        Arrays.stream(constructors)
            .filter(c -> c.isAnnotationPresent(OblivionConstructorInject.class))
            .findFirst();
    return constructorToInject.orElse(constructors[0]);
  }

  public Map<?, Object> getAllSingletonBeans() {
    if (singletonBeans.isEmpty()) {
      return null;
    }
    return singletonBeans;
  }

  public Map<String, PrototypeBeanMetadata> getAllPrototypenBeans() {
    if (prototypeBeans.isEmpty()) {
      return null;
    }
    return prototypeBeans;
  }

  public List<Pair<Object, Method>> getPreDestroyMethods() {
    return this.preDestroyMethods;
  }

  public List<Pair<Object, Method>> getPreShutdownMethods() {
    return this.preShutdownMethods;
  }

  public List<Pair<Object, Method>> getPostShutdownMethods() {
    return this.postShutdownMethods;
  }

  public void clearPreDestroyMap() {
    this.preDestroyMethods.clear();
  }

  public void clearPreShutdownMap() {
    this.preShutdownMethods.clear();
  }

  public void clearPostShutdownMap() {
    this.postShutdownMethods.clear();
  }

  public void clearSingletonBeansMap() {
    this.singletonBeans.clear();
  }

  public void clearPrototypeBeansMap() {
    this.prototypeBeans.clear();
  }
}
