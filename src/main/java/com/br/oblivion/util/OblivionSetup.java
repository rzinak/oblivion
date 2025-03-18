package com.br.oblivion.util;

import com.br.oblivion.bean.PrototypeBean;
import com.br.oblivion.bean.SingletonBean;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.lifecycle.Shutdown;

public class OblivionSetup {
  public static void init(Object appInstance) throws Exception {
    try {
      Shutdown shutdownHook = new Shutdown();
      BeansContainer beansContainer = new BeansContainer();
      shutdownHook.attachShutdown(beansContainer);
      SingletonBean singletonBean = new SingletonBean();
      PrototypeBean prototypeBean = new PrototypeBean();

      Inject.inject(appInstance, singletonBean, prototypeBean, beansContainer);
    } catch (Exception ex) {
      throw new Exception("Failed to initialize oblivion");
    }
  }
}
