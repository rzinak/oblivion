package com.br.samples.productApp.repository;

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
  public void save(Product product) {
    // System.out.println("[DB REPO] Saving product to database: " + product);
    database.put(product.getId(), product);
  }

  @Override
  public Optional<Product> findById(String id) {
    // System.out.println("[DB REPO] Finding product by ID in database: " + id);
    return Optional.ofNullable(database.get(id));
  }

  // when its an implementation of an interface, we target the method in the repository
  // @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  // public void beforeFindAll() {
  //   System.out.println("[PROXY BEFORE] LOGGING BEFORE findAll");
  // }

  // @OblivionAfter(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  // public void afterFindAll() {
  //   System.out.println("[AFTER ADVICE! Logging after 'findAll']");
  // }

  // @OblivionAfterThrowing(target =
  // "com.br.samples.productApp.repository.ProductRepository.findAll")
  // public void afterThrowingFindAll() {
  //   System.out.println("[AFTER THROWING ADVICE! Logging after throwing 'findAll']");
  // }

  @Override
  // @OblivionLoggable
  public List<Product> findAll() {
    System.out.println("[DB REPO] Finding all products in database.");
    // int x = 1 / 0; // just testing a throw for AfterThrowing
    return new ArrayList<>(database.values());
  }
}
