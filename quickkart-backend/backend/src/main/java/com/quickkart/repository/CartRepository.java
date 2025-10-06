package com.quickkart.repository;

import com.quickkart.entity.CartItem;
import com.quickkart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
