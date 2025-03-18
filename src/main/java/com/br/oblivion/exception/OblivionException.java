package com.br.oblivion.exception;

public class OblivionException extends Exception {
  public OblivionException(String message) {
    super(message);
  }

  public OblivionException(String message, Throwable cause) {
    super(message, cause);
  }
}
