package com.br.autowired.lifecycle;

import com.br.autowired.container.BeansContainer;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.Map;

public class Shutdown {
  public void attachShutdown(BeansContainer container) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                for (Map.Entry<Object, Method> entry : container.getPreDestroyMethods()) {
                  try {
                    ReflectionUtils.runPreDestroyMethod(entry.getKey(), entry.getValue());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreDestroy method '%s' in class"
                                + " '%s': %s",
                            entry.getValue().getName(),
                            entry.getKey().getClass().getSimpleName(),
                            ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                for (Map.Entry<Object, Method> entry : container.getPreShutdownMethods()) {
                  try {
                    ReflectionUtils.runPreShutdownMethod(entry.getKey(), entry.getValue());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getValue().getName(),
                            entry.getKey().getClass().getSimpleName(),
                            ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPreDestroyMap();
                container.clearSingletonBeansMap();
                container.clearPrototypeBeansMap();

                for (Map.Entry<Object, Method> entry : container.getPosShutdownMethods()) {
                  try {
                    ReflectionUtils.runPostShutdownMethod(entry.getKey(), entry.getValue());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPostShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getValue().getName(),
                            entry.getKey().getClass().getSimpleName(),
                            ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPostShutdownMap();
              }
            });
  }
}
