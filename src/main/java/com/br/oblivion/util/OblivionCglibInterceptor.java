package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.container.BeansContainer;
import java.lang.reflect.Method;
import java.util.List;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class OblivionCglibInterceptor implements MethodInterceptor {
  private final Object originalTarget;
  private boolean isClassLoggable;

  public OblivionCglibInterceptor(Object originalTarget, boolean isClassLoggable) {
    this.originalTarget = originalTarget;
    this.isClassLoggable = isClassLoggable;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    try {
      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();

      if (BeansContainer.beforeAdviceMap.containsKey(currentMethod)) {
        List<Method> methodsToCall = BeansContainer.beforeAdviceMap.get(currentMethod);

        for (Method m : methodsToCall) {
          m.invoke(obj, args);
        }
      }

      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[GCLIB PROXY] intercepting method -> " + method.getName());
      }

      Object result = method.invoke(this.originalTarget, args);

      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[CGLIB PROXY] finished method -> " + method.getName());
      }

      return result;
    } catch (Throwable t) {
      System.out.println("[CGLIB PROXY] exception in method -> " + method.getName());
      throw t;
    } finally {
      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();
      if (BeansContainer.afterAdviceMap.containsKey(currentMethod)) {
        List<Method> methodsToCall = BeansContainer.afterAdviceMap.get(currentMethod);

        for (Method m : methodsToCall) {
          m.invoke(obj, args);
        }
      }
    }
  }
}
