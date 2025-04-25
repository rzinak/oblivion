package com.br.samples.productApp.repository;

import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionBefore;
import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import com.br.samples.productApp.service.DefaultProductService;
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
    DefaultProductService.show();
    database.put(product.getId(), product);
  }

  @Override
  public Optional<Product> findById(String id) {
    // System.out.println("[DB REPO] Finding product by ID in database: " + id);
    return Optional.ofNullable(database.get(id));
  }

  // now instead of setting the function in the interface here, we must pass the current
  // implementation, since im no more running function based on the class i'm at, but instead
  // im storing the object instance in a map, we can see this in the beginning of the BeansContainer
  // file:
  //
  // public static Map<String, List<Pair<Method, Object>>> beforeAdviceMap = new
  // ConcurrentHashMap<>();
  //
  // if an interface is provided as a target, oblivion wont be able to run, because theres no
  // way to run a method on a interface
  @OblivionBefore(target = "com.br.samples.productApp.repository.DatabaseProductRepository.findAll")
  public void beforeFindAll() {
    System.out.println("[PROXY BEFORE 1] LOGGING BEFORE findAll");
  }

  @OblivionBefore(target = "com.br.samples.productApp.repository.DatabaseProductRepository.findAll")
  public void beforeFindAll2() {
    System.out.println("[PROXY BEFORE 2] LOGGING BEFORE findAll");
  }

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
