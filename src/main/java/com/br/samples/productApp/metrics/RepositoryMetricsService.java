package com.br.samples.productApp.metrics;

import com.br.oblivion.annotations.OblivionService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@OblivionService
public class RepositoryMetricsService {
  private final Map<String, AtomicInteger> saveCounts = new ConcurrentHashMap<>();
  private final Map<String, AtomicInteger> findCounts = new ConcurrentHashMap<>();

  // its called by BeanPostProcessor to register a repository
  public void registerRepository(String beanName) {
    System.out.println("[METRICS SERVICE] registering repository for tracking -> " + beanName);
    saveCounts.putIfAbsent(beanName, new AtomicInteger(0));
    findCounts.putIfAbsent(beanName, new AtomicInteger(0));
  }

  // increment save count (later i believe it would be called by a proxy)
  public void incrementSaveCount(String beanName) {
    saveCounts.computeIfPresent(
        beanName,
        (key, counter) -> {
          counter.incrementAndGet();
          return counter;
        });
  }

  // increment find count (also called by a proxy later)
  public void incrementFindCount(String beanName) {
    findCounts.computeIfPresent(
        beanName,
        (key, counter) -> {
          counter.incrementAndGet();
          return counter;
        });
  }

  // this can be called from another bean
  public void displayMetrics() {
    System.out.println("\n***** Repository Metrics *****");
    if (saveCounts.isEmpty() && findCounts.isEmpty()) {
      System.out.println("No repositories registered for tracking yet...");
      return;
    }
    saveCounts.forEach(
        (beanName, count) -> {
          System.out.printf(
              "Repo: %-30s | Save Calls: %d | Find Calls: %d%n",
              beanName, count.get(), findCounts.getOrDefault(beanName, new AtomicInteger(0)).get());
        });
    System.out.println("\n******************************");
  }
}
