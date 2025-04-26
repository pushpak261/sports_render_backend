package com.ecommerce.sportshub.service.interf;

import com.ecommerce.sportshub.dto.OrderRequest;
import com.ecommerce.sportshub.dto.Response;
import com.ecommerce.sportshub.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderItemService {
    Response placeOrder(OrderRequest orderRequest);
    Response updateOrderItemStatus(Long orderItemId, String status);
    Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable);
}
