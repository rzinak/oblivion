package com.br.service;

import java.util.ArrayList;
import java.util.List;

public class CountryService {
  private final List<String> countries = new ArrayList<>();

  public void addCountry(String country) {
    countries.add(country);
  }

  public List<String> getCountries() {
    return countries;
  }
}
