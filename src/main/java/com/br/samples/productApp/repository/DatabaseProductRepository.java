package com.br.samples.productApp.repository;

import com.br.oblivion.annotations.OblivionAfter;
import com.br.oblivion.annotations.OblivionAfterReturning;
import com.br.oblivion.annotations.OblivionAfterThrowing;
import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionBefore;
import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@OblivionAspect
@OblivionService(name = "DBREPO")
public class DatabaseProductRepository implements ProductRepository, TrackableRepository {
  private final Map<String, Product> database = new ConcurrentHashMap<>();

  @Override
  @OblivionLoggable
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
  @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void beforeFindAll() {
    System.out.println("[PROXY BEFORE 1] LOGGING BEFORE findAll");
  }

  @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void beforeFindAll2() {
    System.out.println("[PROXY BEFORE 2] LOGGING BEFORE findAll");
  }

  @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.save")
  public void beforeSave() {
    System.out.println("[PROXY BEFORE 3 - SAVE] LOGGING BEFORE save");
  }

  @OblivionAfter(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterFindAll() {
    System.out.println("[AFTER ADVICE 1! Logging after 'findAll']");
  }

  @OblivionAfter(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterFindAll2() {
    System.out.println("[AFTER ADVICE 2! Logging after 'findAll']");
  }

  @OblivionAfterThrowing(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterThrowingFindAll() {
    System.out.println("[AFTER THROWING ADVICE 1! Logging after throwing 'findAll']");
  }

  @OblivionAfterThrowing(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterThrowingFindAll2() {
    System.out.println("[AFTER THROWING ADVICE 2! Logging after throwing 'findAll']");
  }

  @OblivionAfterReturning(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterReturningFindAll() {
    System.out.println("[AFTER RETURNING ADVICE 1! Logging after returning 'findAll']");
  }

  @OblivionAfterReturning(target = "com.br.samples.productApp.repository.ProductRepository.findAll")
  public void afterReturningFindAll2() {
    System.out.println("[AFTER RETURNING ADVICE 2! Logging after returning 'findAll']");
  }

  @Override
  @OblivionLoggable
  public List<Product> findAll() {
    System.out.println("[DB REPO] Finding all products in database.");
    // int x = 1 / 0; // just testing a throw for AfterThrowing
    return new ArrayList<>(database.values());
  }
}
