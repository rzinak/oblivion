package com.br.oblivion.bean;

import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;

public class PrototypeBean {
  public <T> Object injectConstructor(
      Class<T> clazz, String identifier, BeansContainer beansContainer) throws Exception {
    try {
      return beansContainer.resolveDependency(clazz, identifier);

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
      throw new OblivionException("Error during prototype bean initialization: " + ex.getMessage());
    }
  }
}
