package com.br;

import com.br.oblivion.util.OblivionSetup;

public class Oblivion {

  // @OblivionWire static TaskCli taskCli;

  public static void main(String[] args) throws Exception {
    try {
      OblivionSetup.preLoadConfigFile();
      OblivionSetup.init();

      // NOTE: if using the oblivion.config file to wire a bean, obviously
      // we cannot call methods like this, as there are no visible reference
      // to taskCli.
      // so to execute a method in this case, we can leverage the use of
      // bean lifecycles like @OblivionPostConstruct, for example.
      // taskCli.run();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
