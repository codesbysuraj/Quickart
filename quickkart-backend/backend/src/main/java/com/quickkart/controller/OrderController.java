package com.quickkart.controller;

import com.quickkart.entity.*;
import com.quickkart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get orders for a user
    @GetMapping("/{username}")
    public ResponseEntity<?> getOrders(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
            List<Map<String, Object>> ordersWithUser = new ArrayList<>();
            for (Order order : orders) {
                OrderAddress addr = order.getOrderAddress();
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("status", order.getStatus());
                orderMap.put("createdAt", order.getCreatedAt());
                orderMap.put("totalAmount", order.getTotalAmount());
                
                Map<String, Object> addressMap = null;
                if (addr != null) {
                    addressMap = new HashMap<>();
                    addressMap.put("fullAddress", addr.getFullAddress());
                    addressMap.put("pincode", addr.getPincode());
                    addressMap.put("phone", addr.getPhone());
                }
                
                Map<String, Object> entry = new HashMap<>();
                entry.put("order", orderMap);
                entry.put("orderAddress", addressMap);
                ordersWithUser.add(entry);
            }
            return ResponseEntity.ok(ordersWithUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch orders: " + e.getMessage());
        }
    }

    // Place an order from cart
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<CartItem> cartItems = cartRepository.findByUser(user);

            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            // Calculate total
            double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

            // Get address info from request
            Map<String, Object> addressMap = (Map<String, Object>) request.get("address");
            String fullAddress = addressMap != null ? (String) addressMap.getOrDefault("fullAddress", "") : user.getAddresses().isEmpty() ? "" : user.getAddresses().get(0).getFullAddress();
            String pincode = addressMap != null ? (String) addressMap.getOrDefault("pincode", "") : user.getAddresses().isEmpty() ? user.getPincode() : user.getAddresses().get(0).getPincode();
            String phone = addressMap != null ? (String) addressMap.getOrDefault("phone", "") : user.getAddresses().isEmpty() ? user.getPhone() : user.getAddresses().get(0).getPhone();
            OrderAddress orderAddress = new OrderAddress(fullAddress, pincode, phone);
            // Create order with address
            Order order = new Order(user, "PLACED", totalAmount, orderAddress);
            Order savedOrder = orderRepository.save(order);

            // Create order items from cart and reduce stock
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                
                // Check if enough stock available
                if (product.getStock() < cartItem.getQuantity()) {
                    return ResponseEntity.badRequest()
                        .body("Insufficient stock for product: " + product.getName());
                }
                
                // Reduce stock
                int oldStock = product.getStock();
                product.setStock(oldStock - cartItem.getQuantity());
                productRepository.save(product); // Save updated stock
                
                OrderItem orderItem = new OrderItem(
                    savedOrder,
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
                );
                orderItemRepository.save(orderItem);
                
                System.out.println("✅ Stock reduced for product: " + product.getName() + 
                                 " | Old stock: " + oldStock + 
                                 " | Quantity ordered: " + cartItem.getQuantity() + 
                                 " | New stock: " + product.getStock());
            }

            // Clear cart
            cartRepository.deleteByUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to place order: " + e.getMessage());
        }
    }

    // Get order details with items
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

            Map<String, Object> response = Map.of(
                "order", order,
                "items", orderItems
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch order details: " + e.getMessage());
        }
    }

    // Get orders containing vendor's products
    @GetMapping("/vendor/{username}")
    public ResponseEntity<?> getVendorOrders(@PathVariable String username) {
        try {
            User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

            // Get all order items with vendor's products
            List<OrderItem> vendorOrderItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getProduct().getVendor() != null && 
                               item.getProduct().getVendor().getId().equals(vendor.getId()))
                .toList();

            // Get unique order IDs
            List<Long> orderIds = vendorOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .toList();

            // Get full order details for each order
            List<Map<String, Object>> ordersWithDetails = orderIds.stream().map(orderId -> {
                Order order = orderRepository.findById(orderId).orElse(null);
                List<OrderItem> items = orderItemRepository.findByOrder(order);
                return Map.of(
                    "order", order,
                    "items", items,
                    "customer", order != null ? order.getUser() : null
                );
            }).toList();

            return ResponseEntity.ok(ordersWithDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to fetch vendor orders: " + e.getMessage());
        }
    }

    // Update order status (for vendors)
    @PutMapping("/status/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            String newStatus = request.get("status");
            order.setStatus(newStatus);
            orderRepository.save(order);
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update order status: " + e.getMessage());
        }
    }
}
