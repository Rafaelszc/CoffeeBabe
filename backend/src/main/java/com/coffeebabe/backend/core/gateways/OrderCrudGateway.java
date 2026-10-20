package com.coffeebabe.backend.core.gateways;

import com.coffeebabe.backend.core.entities.Order;
import com.coffeebabe.backend.core.entities.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface OrderCrudGateway {
    void saveOrder(Order order);
    Optional<Order> findOrder(UUID orderId);
    PageResult<Order> findOrdersByUser(UUID userId, int page, int size);
    PageResult<Order> findAllOrders(int page, int size);
}
