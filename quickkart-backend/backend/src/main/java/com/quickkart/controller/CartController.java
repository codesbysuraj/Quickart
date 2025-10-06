package com.quickkart.controller;

import com.quickkart.entity.CartItem;
import com.quickkart.entity.Product;
import com.quickkart.entity.User;
import com.quickkart.repository.CartRepository;
import com.quickkart.repository.ProductRepository;
import com.quickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get cart items for a user
    @GetMapping("/{username}")
    public ResponseEntity<?> getCart(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            List<CartItem> cartItems = cartRepository.findByUser(user);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch cart: " + e.getMessage());
        }
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

            // Check if enough stock is available
            if (product.getStock() == null || product.getStock() < quantity) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Not enough stock available");
            }

            // Decrement product stock
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            CartItem cartItem = new CartItem(user, product, quantity);
            CartItem saved = cartRepository.save(cartItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to add to cart: " + e.getMessage());
        }
    }

    // Remove item from cart
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            if (cartRepository.existsById(id)) {
                CartItem cartItem = cartRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
                Product product = cartItem.getProduct();
                Integer quantity = cartItem.getQuantity();
                // Increment product stock
                product.setStock(product.getStock() + quantity);
                productRepository.save(product);
                cartRepository.deleteById(id);
                return ResponseEntity.ok("Item removed from cart");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to remove from cart: " + e.getMessage());
        }
    }

    // Update cart item quantity
    @PutMapping("/{id}/quantity")
    public ResponseEntity<?> updateCartQuantity(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            CartItem cartItem = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
            
            // Validate quantity
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body("Quantity must be greater than 0");
            }
            
            // Check stock availability
            if (quantity > cartItem.getProduct().getStock()) {
                return ResponseEntity.badRequest()
                    .body("Requested quantity exceeds available stock");
            }
            
            cartItem.setQuantity(quantity);
            CartItem updated = cartRepository.save(cartItem);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update cart quantity: " + e.getMessage());
        }
    }

    // Clear cart for a user
    @DeleteMapping("/clear/{username}")
    public ResponseEntity<?> clearCart(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            cartRepository.deleteByUser(user);
            return ResponseEntity.ok("Cart cleared");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to clear cart: " + e.getMessage());
        }
    }
}
