package com.br.samples.productApp.repository;

import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@OblivionService(name = "DBREPO")
public class DatabaseProductRepository implements ProductRepository, TrackableRepository {
  private final Map<String, Product> database = new ConcurrentHashMap<>();

  @Override
  public void save(Product product) {
    System.out.println("[DB REPO] Saving product to database: " + product);
    database.put(product.getId(), product);
  }

  @Override
  public Optional<Product> findById(String id) {
    System.out.println("[DB REPO] Finding product by ID in database: " + id);
    return Optional.ofNullable(database.get(id));
  }

  @Override
  public List<Product> findAll() {
    System.out.println("[DB REPO] Finding all products in database.");
    return new ArrayList<>(database.values());
  }
}
