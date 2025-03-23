package com.br;

import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.util.OblivionConfig;
import com.br.oblivion.util.OblivionSetup;
import com.br.samples.testAppTaskManager.cli.*;
import java.util.Map.Entry;
import java.util.Properties;

public class Oblivion {

  // @OblivionWire static TaskCli taskCli;

  public static void main(String[] args) throws Exception {
    preLoadConfigFile();

    try {
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

  public static void preLoadConfigFile() throws Exception, OblivionException {
    Properties oblivionConfigProperties;

    try {
      oblivionConfigProperties = OblivionConfig.loadProperties();
      Class<?> currClass = null;
      String currIdentifier = null;

      try {
        for (Entry<Object, Object> entry : oblivionConfigProperties.entrySet()) {
          System.out.println("key: " + entry.getKey());
          System.out.println("val: " + entry.getValue());
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
              System.out.println("error instantiating class from config file");
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

  public static void testing() {
    // System.out.println("TASK SERVICE: " + taskService);
    // taskService.setTaskName("Code PROTO 1");
    // taskService.setIsAvailable(false);
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getTaskName());
    // System.out.println("CURRENT TASK SERVICE BOOL VAL: " + taskService.getIsAvailable());
    // System.out.println("CURRENT TASK SERVICE BOXED BOOL VAL: " + taskService.getIsRegistered());
    // System.out.println("CURRENT TASK SERVICE NORMAL INT VAL: " + taskService.getNormalInt());
    // System.out.println("CURRENT TASK SERVICE BOXED INT VAL: " + taskService.getBoxedInt());
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getStringText());
    // System.out.println("CURRENT TASK SERVICE LIST VAL: " + taskService.getListString());
    // System.out.println("CURRENT TASK SERVICE MAP VAL: " + taskService.getMapStringString());
  }
}
