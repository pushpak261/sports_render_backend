package com.ecommerce.sportshub.repository;

import com.ecommerce.sportshub.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
