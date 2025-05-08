package com.br.samples.productApp.cli;

import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.productApp.domain.Product;
import com.br.samples.productApp.service.DefaultProductService;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@OblivionAspect
@OblivionService
public class ProductCLI {

  private final DefaultProductService productService;
  private final Scanner scanner;

  // @OblivionPostInitialization
  // public void t() {
  //   System.out.println("running post init");
  // }

  public ProductCLI(DefaultProductService productService) {
    // System.out.println(
    //     "ProductCLI created with service: " + productService.getClass().getSimpleName());
    this.productService = productService;
    this.scanner = new Scanner(System.in);
  }

  // @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.save")
  // public void beforeOut() {
  //   System.out.println("BEFORE SAVE, CALLED FROM PRODUCTCLI");
  // }

  // @OblivionBefore(target = "com.br.samples.productApp.repository.ProductRepository.save")
  // // public void beforeSave(OblivionJoinPoint jp) {
  // public void beforeSaveMod(OblivionJoinPoint jp) throws Exception {
  //   Object[] args = jp.getArgs();
  //   if (args != null && args.length > 0 && args[0] instanceof Product) {
  //     Product productBeingSaved = (Product) args[0];
  //     System.out.println(
  //         "[MOD] => [ASPECT LOG] Attempting to save Product ID: "
  //             + productBeingSaved.getId()
  //             + ", product name is "
  //             + productBeingSaved.getName());
  //   }
  //
  //   Method method = jp.getMethod();
  //   System.out.println("[MOD] => [ASPECT LOG] method -> " + method.getName());
  //   Object target = jp.getTarget();
  //   System.out.println("[MOD] => [ASPECT LOG] target -> " + target.getClass().getName());
  // }

  // @OblivionBefore(target = "com.br.samples.productApp.service.DefaultProductService.show")
  // public void beforeShow() {
  //   System.out.println("BEFORE SHOW, BUT CALLED FROM OUTSIDE");
  // }

  public void run() {
    System.out.println("\n--- Product Management ---");
    String choice;
    do {
      System.out.println("\n1. Add Product");
      System.out.println("2. Find Product by ID");
      System.out.println("3. Show All Products");
      System.out.println("4. Exit");
      System.out.print("> ");
      choice = scanner.nextLine();

      switch (choice) {
        case "1":
          addProduct();
          break;
        case "2":
          findProduct();
          break;
        case "3":
          showAllProducts();
          break;
        case "4":
          System.out.println("Exiting Product Management...");
          break;
        default:
          System.out.println("Invalid choice.");
      }
    } while (!choice.equals("4"));
  }

  private void addProduct() {
    System.out.print("Enter product name: ");
    String name = scanner.nextLine();
    if (name != null && !name.trim().isEmpty()) {
      Product p = productService.createProduct(name);
      System.out.println("Product added: " + p);
    } else {
      System.out.println("Product name cannot be empty.");
    }
  }

  private void findProduct() {
    System.out.print("Enter product ID: ");
    String id = scanner.nextLine();
    Optional<Product> productOpt = productService.getProduct(id);
    if (productOpt.isPresent()) {
      System.out.println("Found: " + productOpt.get());
    } else {
      System.out.println("Product not found with ID: " + id);
    }
  }

  private void showAllProducts() {
    List<Product> products = productService.getAllProducts();
    if (products.isEmpty()) {
      System.out.println("No products available.");
    } else {
      System.out.println("--- All Products ---");
      products.forEach(System.out::println);
      System.out.println("--------------------");
    }
  }
}
