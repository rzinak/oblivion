package com.br.samples.productApp.repository;

import com.br.oblivion.annotations.OblivionBefore;
import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// @OblivionAspect
@OblivionService(name = "DBREPO")
public class DatabaseProductRepository implements ProductRepository, TrackableRepository {
  private final Map<String, Product> database = new ConcurrentHashMap<>();

  @Override
  @OblivionLoggable
  public void save(Product product) {
    // System.out.println("[DB REPO] Saving product to database: " + product);
    // DefaultProductService.show();
    database.put(product.getId(), product);
    System.out.println("calling save inside DatabaseProductRepository");
  }

  @OblivionLoggable
  @OblivionBefore(target = "com.br.samples.productApp.repository.DatabaseProductRepository.save")
  public void tFunction() {
    System.out.println("calling tFunction");
  }

  @Override
  public Optional<Product> findById(String id) {
    // System.out.println("[DB REPO] Finding product by ID in database: " + id);
    return Optional.ofNullable(database.get(id));
  }

  @OblivionBefore(target = "com.br.samples.productApp.repository.DatabaseProductRepository.findAll")
  public void beforeFindAll() {
    System.out.println("[PROXY BEFORE 1] LOGGING BEFORE findAll");
  }

  // @OblivionBefore(target =
  // "com.br.samples.productApp.repository.DatabaseProductRepository.findAll")
  // public void beforeFindAll2() {
  //   System.out.println("[PROXY BEFORE 2] LOGGING BEFORE findAll");
  // }

  // @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.save")
  @OblivionBefore(target = "com.br.samples.productApp.repository.DatabaseProductRepository.save")
  public void beforeSave() {
    System.out.println("[PROXY BEFORE 3 - SAVE] LOGGING BEFORE save");
  }

  @Override
  @OblivionLoggable
  public List<Product> findAll() {
    System.out.println("[DB REPO] Finding all products in database.");
    // int x = 1 / 0; // just testing a throw for AfterThrowing
    return new ArrayList<>(database.values());
  }
}
