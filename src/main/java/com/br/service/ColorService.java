package com.br.service;

import com.br.App.Oblivion;
import java.util.List;

public class ColorService {
  private List<String> colors;

  @Oblivion private String myFavColor;

  public void addColor(String color) {
    colors.add(color);
  }

  public List<String> getColors() {
    return colors;
  }
}
