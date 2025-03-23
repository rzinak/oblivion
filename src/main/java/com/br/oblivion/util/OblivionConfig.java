package com.br.oblivion.util;

import com.br.oblivion.exception.OblivionException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class OblivionConfig {
  public static Properties loadProperties() throws OblivionException {
    Properties oblivionConfigProperties = new Properties();
    String propertiesFilePath = "oblivion.config";

    try (FileInputStream input = new FileInputStream(propertiesFilePath)) {
      oblivionConfigProperties.load(input);
      return oblivionConfigProperties;
    } catch (IOException ex) {
      if (ex instanceof FileNotFoundException) {
        throw new OblivionException("'oblivion.config' file was not found");
      }
      throw new OblivionException(ex.getMessage());
    }
  }
}
