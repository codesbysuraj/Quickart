package com.quickkart.repository;

import com.quickkart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPincode(String pincode);
    List<Product> findByPincodeAndCategory(String pincode, String category);
    List<Product> findByCategory(String category);
    List<Product> findByVendorId(Long vendorId);
}
