package com.quickkart.controller;

import com.quickkart.entity.User;
import com.quickkart.entity.Product;
import com.quickkart.repository.UserRepository;
import com.quickkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;

    // GET /api/users/{username}
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/users/{username}
    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody java.util.Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Update basic fields
        if (updates.containsKey("fullName")) {
            user.setFullName((String) updates.get("fullName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        // Track if pincode changed for vendors
        boolean pincodeChanged = false;
        String newPincode = null;
        
        if (updates.containsKey("pincode")) {
            newPincode = (String) updates.get("pincode");
            String oldPincode = user.getPincode();
            
            // Check if pincode actually changed
            if (!newPincode.equals(oldPincode)) {
                pincodeChanged = true;
            }
            
            user.setPincode(newPincode);
        }
        if (updates.containsKey("city")) {
            user.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("dateOfBirth")) {
            user.setDateOfBirth((String) updates.get("dateOfBirth"));
        }

        // Handle password change
        if (updates.containsKey("currentPassword") && updates.containsKey("newPassword")) {
            String currentPassword = (String) updates.get("currentPassword");
            String newPassword = (String) updates.get("newPassword");
            
            // Verify current password
            if (!user.getPassword().equals(currentPassword)) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            
            // Update to new password
            user.setPassword(newPassword);
        }

        // Save updated user
        User savedUser = userRepository.save(user);
        
        // If vendor changed pincode, update all their products
        if (pincodeChanged && "VENDOR".equals(user.getRole())) {
            List<Product> vendorProducts = productRepository.findByVendorId(user.getId());
            int updatedCount = 0;
            
            for (Product product : vendorProducts) {
                product.setPincode(newPincode);
                productRepository.save(product);
                updatedCount++;
            }
            
            System.out.println("✓ Vendor " + user.getUsername() + " changed pincode from " + 
                             "their old pincode to " + newPincode);
            System.out.println("✓ Updated " + updatedCount + " products to new pincode: " + newPincode);
        }
        
        return ResponseEntity.ok(savedUser);
    }
}
