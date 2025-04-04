package com.br.oblivion.util;

import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.lifecycle.Shutdown;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OblivionSetup {

  public static void init() throws Exception {
    try {
      Object appInstance = null;
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      Package mPackage = null;

      for (StackTraceElement sTraceElement : stackTraceElements) {
        if (sTraceElement.getMethodName().equals("main")) {
          Class<?> mainClass = Class.forName(sTraceElement.getClassName());
          mPackage = mainClass.getPackage();
          appInstance = mainClass.newInstance();
        }
      }

      String rootPackage = (mPackage != null) ? mPackage.getName() : "";

      Shutdown shutdownHook = new Shutdown();
      BeansContainer beansContainer = new BeansContainer();
      ThreadPoolExecutor threadPoolExecutor = setupThreadPool();
      shutdownHook.attachShutdown(beansContainer, threadPoolExecutor);
      Inject.inject(appInstance, beansContainer, threadPoolExecutor, rootPackage);

    } catch (Exception ex) {
      throw new Exception("Failed to initialize oblivion -> " + ex.getMessage());
    }
  }

  public static ThreadPoolExecutor setupThreadPool() throws OblivionException, Exception {
    Properties threadPoolProperties;
    try {
      threadPoolProperties = ThreadPoolConfig.loadProperties();
      try {
        TimeUnit unit = TimeUnit.SECONDS;
        int corePoolSize = Integer.parseInt(threadPoolProperties.getProperty("CORE_POOL_SIZE"));
        int maxPoolSize = Integer.parseInt(threadPoolProperties.getProperty("MAX_POOL_SIZE"));
        int keepAliveTime = Integer.parseInt(threadPoolProperties.getProperty("KEEP_ALIVE_TIME"));
        int workQueueVal = Integer.parseInt(threadPoolProperties.getProperty("WORK_QUEUE_VAL"));
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(workQueueVal);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
      } catch (Exception ex) {
        throw new Exception(ex.getMessage());
      }
    } catch (OblivionException ex) {
      throw new OblivionException("Error loading thread pool config: " + ex.getMessage());
    }
  }

  public static void preLoadConfigFile() throws Exception, OblivionException {
    Properties oblivionConfigProperties;

    try {
      oblivionConfigProperties = OblivionConfig.loadProperties();
      Class<?> currClass = null;
      String currIdentifier = null;

      try {
        for (Entry<Object, Object> entry : oblivionConfigProperties.entrySet()) {
          String currKey = entry.getKey().toString();

          if (currKey.equals("OblivionWire")) {
            try {
              String currVal = oblivionConfigProperties.getProperty(currKey);
              currClass = Class.forName(currVal);
              currIdentifier = currClass.getSimpleName();
              char c[] = currIdentifier.toCharArray();
              c[0] = Character.toLowerCase(c[0]);
              currIdentifier = new String(c);
              BeansContainer.registerConfigBean(currIdentifier, currClass);
            } catch (Exception ex) {
              throw new Exception("Error instantiating class from config file: " + ex.getMessage());
            }
          }
        }
      } catch (Exception ex) {
        throw new Exception("Failed to read config file: " + ex.getMessage());
      }
    } catch (OblivionException ex) {
      throw new OblivionException("Error loading oblivion config file: " + ex.getMessage());
    }
  }
}
