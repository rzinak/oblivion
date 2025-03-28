package com.br.oblivion.bean;

import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;

// TODO: gotta refactor the 'init inner deps' logic
public class SingletonBean {
  public <T> Object injectConstructor(
      Class<T> clazz, String classIdentifier, BeansContainer beansContainer) throws Exception {

    if (beansContainer.getSingletonBean(clazz.getSimpleName()) != null) {
      return null;
    }

    try {
      return beansContainer.resolveDependency(clazz, classIdentifier);

      // NOTE: registration and lifecycle calls should now happen *inside* resolveDependency

      // NOTE:
      // if resolveDependency doesn't register, i might register here, but it's
      // cleaner if resolveDependency handles its own registration upon successful
      // creation like i plan to do.
      // beansContainer.registerSingletonBean(identifier, instance);

      // NOTE:
      // initial field injection and post-init might still happen here,
      // OR preferably inside resolveDependency after instantiation and registration.
      // ReflectionUtils.runPostInitializationMethods(clazz, instance, threadPoolExecutor);
      // ReflectionUtils.registerPersistentBeanLifecycles(clazz, instance, beansContainer);
    } catch (Exception ex) {
      throw new OblivionException("Error during singleton bean initialization: " + ex.getMessage());
    }
  }
}
