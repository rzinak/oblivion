package com.br.samples.productApp.service;

import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionQualifier;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import com.br.samples.productApp.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OblivionService
@OblivionPrototype
public class DefaultProductService {

  private final ProductRepository repository;

  public DefaultProductService(@OblivionQualifier(name = "DBREPO") ProductRepository repository) {
    System.out.println(
        "DefaultProductService created with repository: " + repository.getClass().getSimpleName());
    this.repository = repository;
  }

  public Product createProduct(String name) {
    String id = UUID.randomUUID().toString().substring(0, 8);
    Product newProduct = new Product(id, name);
    repository.save(newProduct);
    return newProduct;
  }

  public Optional<Product> getProduct(String id) {
    return repository.findById(id);
  }

  public List<Product> getAllProducts() {
    return repository.findAll();
  }
}
