package com.br.oblivion.util;

import com.br.oblivion.exception.OblivionException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ThreadPoolConfig {
  public static Properties loadProperties() throws OblivionException {
    Properties threadPoolProperties = new Properties();
    String propertiesFilePath = "oblivion.threadpool.properties";

    try (FileInputStream input = new FileInputStream(propertiesFilePath)) {
      threadPoolProperties.load(input);
      return threadPoolProperties;
    } catch (IOException ex) {
      if (ex instanceof FileNotFoundException) {
        throw new OblivionException("'oblivion.threadpool.properties' file was not found");
      }
      throw new OblivionException(ex.getMessage());
    }
  }
}
