package com.br.samples.productApp.repository;

import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@OblivionService
public class InMemoryProductRepository implements ProductRepository, TrackableRepository {

  // @OblivionPostInitialization
  // public void sayHi() {
  //   System.out.println("Running inside InMemoryProductRepository");
  // }

  private final Map<String, Product> memoryStore = new ConcurrentHashMap<>();

  @Override
  public void save(Product product) {
    System.out.println("[MEM REPO] Saving product to memory: " + product);
    memoryStore.put(product.getId(), product);
  }

  @Override
  public Optional<Product> findById(String id) {
    System.out.println("[MEM REPO] Finding product by ID in memory: " + id);
    return Optional.ofNullable(memoryStore.get(id));
  }

  @Override
  public List<Product> findAll() {
    System.out.println("[MEM REPO] Finding all products in memory.");
    return new ArrayList<>(memoryStore.values());
  }
}
