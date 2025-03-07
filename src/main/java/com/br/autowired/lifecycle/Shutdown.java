package com.br.autowired.lifecycle;

import com.br.autowired.annotations.OblivionPostShutdown;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.container.Pair;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Method;

public class Shutdown {
  public void attachShutdown(BeansContainer container) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  ReflectionUtils.validateAndSortMethods(
                      container.getPreDestroyMethods(), OblivionPreDestroy.class);

                  ReflectionUtils.validateAndSortMethods(
                      container.getPreShutdownMethods(), OblivionPreShutdown.class);

                  ReflectionUtils.validateAndSortMethods(
                      container.getPostShutdownMethods(), OblivionPostShutdown.class);
                } catch (OblivionException ex) {
                  System.err.println("SHUTDOWN ABORTED: Configuration errors detected");
                  System.out.println(ex.getMessage());
                  return;
                }

                for (Pair<Object, Method> entry : container.getPreDestroyMethods()) {
                  try {
                    ReflectionUtils.runPreDestroyMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreDestroy method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                for (Pair<Object, Method> entry : container.getPreShutdownMethods()) {
                  try {
                    ReflectionUtils.runPreShutdownMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPreDestroyMap();
                container.clearSingletonBeansMap();
                container.clearPrototypeBeansMap();

                for (Pair<Object, Method> entry : container.getPostShutdownMethods()) {
                  try {
                    ReflectionUtils.runPostShutdownMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPostShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPostShutdownMap();
              }
            });
  }
}
