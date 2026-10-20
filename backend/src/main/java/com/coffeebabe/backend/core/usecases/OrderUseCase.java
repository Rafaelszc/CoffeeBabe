package com.coffeebabe.backend.core.usecases;

import com.coffeebabe.backend.core.entities.FulfillmentType;
import com.coffeebabe.backend.core.entities.Order;
import com.coffeebabe.backend.core.entities.OrderStatus;
import com.coffeebabe.backend.core.entities.PageResult;

import java.util.UUID;

public interface OrderUseCase {
    Order placeOrder(UUID userId, FulfillmentType fulfillment, String address);
    PageResult<Order> listOwnOrders(UUID userId, int page, int size);
    PageResult<Order> listAllOrders(int page, int size);
    Order getOrder(UUID requesterId, boolean requesterIsAdmin, UUID orderId);
    Order cancelOrder(UUID requesterId, boolean requesterIsAdmin, UUID orderId);
    Order updateStatus(UUID orderId, OrderStatus status);
}
