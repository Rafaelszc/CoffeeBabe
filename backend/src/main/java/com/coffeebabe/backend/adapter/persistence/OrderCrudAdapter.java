package com.coffeebabe.backend.adapter.persistence;

import com.coffeebabe.backend.core.entities.Order;
import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.gateways.OrderCrudGateway;
import com.coffeebabe.backend.infra.persistence.jpa.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderCrudAdapter implements OrderCrudGateway {
    private final OrderRepository orderRepository;

    public OrderCrudAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    public Optional<Order> findOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public PageResult<Order> findOrdersByUser(UUID userId, int page, int size) {
        return toPageResult(orderRepository.findByUserId(userId, newestFirst(page, size)));
    }

    @Override
    public PageResult<Order> findAllOrders(int page, int size) {
        return toPageResult(orderRepository.findAll(newestFirst(page, size)));
    }

    private static Pageable newestFirst(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private static PageResult<Order> toPageResult(Page<Order> result) {
        return new PageResult<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
