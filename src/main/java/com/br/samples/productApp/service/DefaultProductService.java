package com.br.samples.productApp.service;

import com.br.oblivion.annotations.OblivionAfterThrowing;
import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionQualifier;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import com.br.samples.productApp.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OblivionAspect
@OblivionService
@OblivionPrototype
public class DefaultProductService {

  private final ProductRepository repository;

  public DefaultProductService(@OblivionQualifier(name = "DBREPO") ProductRepository repository) {
    // System.out.println(
    //     "DefaultProductService created with repository: " +
    // repository.getClass().getSimpleName());
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

  // @OblivionBefore(target =
  // "com.br.samples.productApp.service.DefaultProductService.getAllProducts")
  // public void beforeGetAllProducts() {
  //   System.out.println("[BEFORE ADVICE! Logging before 'getAllProducts']");
  // }

  // @OblivionAfter(target =
  // "com.br.samples.productApp.service.DefaultProductService.getAllProducts")
  // public void afterGetAllProducts() {
  //   System.out.println("[AFTER ADVICE! Logging after 'getAllProducts']");
  // }

  @OblivionAfterThrowing(
      target = "com.br.samples.productApp.service.DefaultProductService.getAllProducts")
  public void afterThrowingGetAllProducts() {
    System.out.println("[AFTER THROWING ADVICE! Logging after throwing 'getAllProducts']");
  }

  @OblivionLoggable
  public List<Product> getAllProducts() {
    int x = 1 / 0; // just testing a throw for AfterThrowing
    return repository.findAll();
  }
}
