package com.quickkart.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    // Store the address used for this order
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fullAddress", column = @Column(name = "full_address")),
        @AttributeOverride(name = "pincode", column = @Column(name = "pincode")),
        @AttributeOverride(name = "phone", column = @Column(name = "phone"))
    })
    private OrderAddress orderAddress;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String status; // PLACED, CONFIRMED, DELIVERED, CANCELLED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Double totalAmount;

    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "PLACED";
    }

    public Order(User user, String status, Double totalAmount, OrderAddress orderAddress) {
        this.user = user;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.orderAddress = orderAddress;
    }
    public OrderAddress getOrderAddress() { return orderAddress; }
    public void setOrderAddress(OrderAddress orderAddress) { this.orderAddress = orderAddress; }

    // Getters and Setters
    public Long getId() { return id; }
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}
