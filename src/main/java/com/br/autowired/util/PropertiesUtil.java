package com.br.autowired.util;

import com.br.autowired.exception.OblivionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
  public static Properties loadProperties() throws OblivionException {
    Properties oblivionProperties = new Properties();
    String oblivionPropertiesFilePath = "oblivion.properties";

    try (FileInputStream input = new FileInputStream(oblivionPropertiesFilePath)) {
      oblivionProperties.load(input);
      return oblivionProperties;
    } catch (IOException ex) {
      throw new OblivionException("Error loading properties file: " + ex.getMessage());
    }
  }
}
