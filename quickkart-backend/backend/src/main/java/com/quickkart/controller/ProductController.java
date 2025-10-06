package com.quickkart.controller;

import com.quickkart.entity.Product;
import com.quickkart.entity.User;
import com.quickkart.repository.ProductRepository;
import com.quickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Get products by pincode (main feature for hyperlocal)
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String category) {
        try {
            List<Product> products;
            
            // If no pincode provided, return all products
            if (pincode == null || pincode.isEmpty()) {
                if (category != null && !category.isEmpty()) {
                    products = productRepository.findByCategory(category);
                } else {
                    products = productRepository.findAll();
                }
            } else {
                // Filter by pincode
                if (category != null && !category.isEmpty()) {
                    products = productRepository.findByPincodeAndCategory(pincode, category);
                } else {
                    products = productRepository.findByPincode(pincode);
                }
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all products (admin/vendor use)
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            return ResponseEntity.ok(productRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Get products by vendor ID
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Product>> getProductsByVendor(@PathVariable Long vendorId) {
        try {
            List<Product> products = productRepository.findByVendorId(vendorId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add new product (vendor operation)
     * Ensures proper vendor association and pincode validation
     * 
     * @param product Product data from request body
     * @return ResponseEntity with created product or error message
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        try {
            // Log incoming product details
            System.out.println("=== Adding New Product ===");
            System.out.println("Product Name: " + product.getName());
            System.out.println("Product Category: " + product.getCategory());
            System.out.println("Product Price: " + product.getPrice());
            System.out.println("Product Stock: " + product.getStock());
            System.out.println("Product Pincode (received): " + product.getPincode());
            
            // Validate and fetch vendor
            if (product.getVendor() == null || product.getVendor().getId() == null) {
                System.err.println("ERROR: Vendor ID is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vendor ID is required to add a product");
            }
            
            Long vendorId = product.getVendor().getId();
            System.out.println("Vendor ID: " + vendorId);
            
            // Fetch the full vendor entity from database
            User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));
            
            System.out.println("Vendor found: " + vendor.getUsername() + " (Role: " + vendor.getRole() + ")");
            System.out.println("Vendor Pincode: " + vendor.getPincode());
            
            // Validate vendor role
            if (!"VENDOR".equals(vendor.getRole())) {
                System.err.println("ERROR: User is not a vendor");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Only vendors can add products");
            }
            
            // Set the full vendor entity (not just ID)
            product.setVendor(vendor);
            
            // If product pincode is missing, use vendor's pincode
            if (product.getPincode() == null || product.getPincode().isEmpty()) {
                product.setPincode(vendor.getPincode());
                System.out.println("Product pincode set from vendor: " + vendor.getPincode());
            }
            
            // Validate required fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Product name is required");
            }
            
            if (product.getPrice() == null || product.getPrice() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Valid product price is required");
            }
            
            // Log final product state before saving
            System.out.println("Saving product with pincode: " + product.getPincode());
            
            // Save product to database
            Product savedProduct = productRepository.save(product);
            
            // Log success
            System.out.println("✓ Product saved successfully!");
            System.out.println("  - Product ID: " + savedProduct.getId());
            System.out.println("  - Product Name: " + savedProduct.getName());
            System.out.println("  - Product Pincode: " + savedProduct.getPincode());
            System.out.println("  - Vendor: " + savedProduct.getVendor().getUsername());
            System.out.println("========================");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
            
        } catch (RuntimeException e) {
            // Handle expected exceptions (e.g., vendor not found)
            System.err.println("ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
                
        } catch (Exception e) {
            // Handle unexpected exceptions
            System.err.println("UNEXPECTED ERROR adding product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to add product: " + e.getMessage());
        }
    }

    // Update product (vendor)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            return productRepository.findById(id)
                .map(existingProduct -> {
                    // Only update fields that are provided (not null)
                    if (product.getName() != null) existingProduct.setName(product.getName());
                    if (product.getCategory() != null) existingProduct.setCategory(product.getCategory());
                    if (product.getPrice() != null) existingProduct.setPrice(product.getPrice());
                    if (product.getPincode() != null) existingProduct.setPincode(product.getPincode());
                    if (product.getDescription() != null) existingProduct.setDescription(product.getDescription());
                    if (product.getImageUrl() != null) existingProduct.setImageUrl(product.getImageUrl());
                    if (product.getStock() != null) existingProduct.setStock(product.getStock());
                    return ResponseEntity.ok(productRepository.save(existingProduct));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update product: " + e.getMessage());
        }
    }

    // Delete product (vendor)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                return ResponseEntity.ok("Product deleted successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete product: " + e.getMessage());
        }
    }
}
