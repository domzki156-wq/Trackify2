package App.dao;

import App.models.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product save(Product p); // create or update
    Optional<Product> findById(String id);
    Optional<Product> findByName(String name); // case-insensitive match helpful
    List<Product> findAll();
    boolean updateStock(String productId, int newStock);
    boolean adjustStock(String productId, int delta); // delta can be negative
    void deleteById(String id);
}
