package com.br.samples.productApp.repository;

import com.br.samples.productApp.domain.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

  void save(Product product);

  Optional<Product> findById(String id);

  List<Product> findAll();
}
