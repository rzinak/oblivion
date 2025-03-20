package com.br.oblivion.util;

import com.br.oblivion.bean.PrototypeBean;
import com.br.oblivion.bean.SingletonBean;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.lifecycle.Shutdown;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OblivionSetup {

  public static void init(Object appInstance) throws Exception {
    try {
      Shutdown shutdownHook = new Shutdown();
      BeansContainer beansContainer = new BeansContainer();
      ThreadPoolExecutor threadPoolExecutor = setupThreadPool();
      shutdownHook.attachShutdown(beansContainer, threadPoolExecutor);
      SingletonBean singletonBean = new SingletonBean();
      PrototypeBean prototypeBean = new PrototypeBean();
      Inject.inject(appInstance, singletonBean, prototypeBean, beansContainer, threadPoolExecutor);
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
}
