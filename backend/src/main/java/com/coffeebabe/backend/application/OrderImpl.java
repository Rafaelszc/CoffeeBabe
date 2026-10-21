package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.*;
import com.coffeebabe.backend.core.exceptions.InvalidDataException;
import com.coffeebabe.backend.core.exceptions.OrderNotFoundException;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.core.gateways.OrderCrudGateway;
import com.coffeebabe.backend.core.usecases.OrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderImpl implements OrderUseCase {
    private final OrderCrudGateway orderCrudGateway;
    private final CartCrudGateway cartCrudGateway;

    public OrderImpl(OrderCrudGateway orderCrudGateway, CartCrudGateway cartCrudGateway) {
        this.orderCrudGateway = orderCrudGateway;
        this.cartCrudGateway = cartCrudGateway;
    }

    // order and cleared cart commit together, or neither does (RF-CART-4)
    @Override
    @Transactional
    public Order placeOrder(UUID userId, FulfillmentType fulfillment, String address) {
        var items = cartCrudGateway.getItems(userId);
        if (items.isEmpty()) {
            throw new InvalidDataException("InvalidDataException");
        }

        var delivery = fulfillment == FulfillmentType.DELIVERY;
        if (delivery && (address == null || address.isBlank())) {
            throw new InvalidDataException("InvalidDataException");
        }

        // prices come from the catalog in the database, never from the client (RF-ORD-4)
        var prices = PriceSummary.of(items, delivery ? PriceSummary.DELIVERY_FEE : BigDecimal.ZERO);
        var order = new Order(userId, fulfillment, delivery ? address.trim() : null, items, prices);

        orderCrudGateway.saveOrder(order);
        cartCrudGateway.clearCart(userId);
        return order;
    }

    @Override
    public PageResult<Order> listOwnOrders(UUID userId, int page, int size) {
        PageResult.checkPaging(page, size);
        return orderCrudGateway.findOrdersByUser(userId, page, size);
    }

    @Override
    public PageResult<Order> listAllOrders(int page, int size) {
        PageResult.checkPaging(page, size);
        return orderCrudGateway.findAllOrders(page, size);
    }

    @Override
    public Order getOrder(UUID requesterId, boolean requesterIsAdmin, UUID orderId) {
        // someone else's order is 404, not 403, so its existence is not revealed (SEC-8)
        return orderCrudGateway.findOrder(orderId)
                .filter(order -> requesterIsAdmin || order.getUserId().equals(requesterId))
                .orElseThrow(() -> new OrderNotFoundException("OrderNotFoundException"));
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID requesterId, boolean requesterIsAdmin, UUID orderId) {
        var order = getOrder(requesterId, requesterIsAdmin, orderId);
        order.moveTo(OrderStatus.CANCELLED);
        orderCrudGateway.saveOrder(order);
        return order;
    }

    @Override
    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus status) {
        var order = getOrder(null, true, orderId);
        order.moveTo(status);
        orderCrudGateway.saveOrder(order);
        return order;
    }
}
