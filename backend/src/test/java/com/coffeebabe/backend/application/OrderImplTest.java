package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.*;
import com.coffeebabe.backend.core.exceptions.InvalidDataException;
import com.coffeebabe.backend.core.exceptions.OrderNotFoundException;
import com.coffeebabe.backend.core.exceptions.OrderStatusConflictException;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.core.gateways.OrderCrudGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderImplTest {
    private final UUID userId = UUID.randomUUID();
    private final List<CartItem> prdCart = List.of(
            new CartItem(new Product("Cappuccino", "d", new BigDecimal("4.00"), ProductCategory.HOT_COFFEE), 2),
            new CartItem(new Product("Mocha", "d", new BigDecimal("4.50"), ProductCategory.HOT_COFFEE), 1));

    private OrderCrudGateway orderGateway;
    private CartCrudGateway cartGateway;
    private OrderImpl orders;

    @BeforeEach
    void setUp() {
        orderGateway = mock(OrderCrudGateway.class);
        cartGateway = mock(CartCrudGateway.class);
        orders = new OrderImpl(orderGateway, cartGateway);
    }

    @Test
    void deliveryOrderUsesServerPricesSnapshotsItemsAndClearsCart() {
        when(cartGateway.getItems(userId)).thenReturn(prdCart);

        var order = orders.placeOrder(userId, FulfillmentType.DELIVERY, "  Rua A, 10  ");

        assertThat(order.getTotal()).isEqualTo(new BigDecimal("32.88"));
        assertThat(order.getDeliveryFee()).isEqualTo(new BigDecimal("20.00"));
        assertThat(order.getAddress()).isEqualTo("Rua A, 10");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).extracting(OrderItem::getProductName, OrderItem::getQuantity)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Cappuccino", 2),
                        org.assertj.core.groups.Tuple.tuple("Mocha", 1));
        var inOrder = inOrder(orderGateway, cartGateway);
        inOrder.verify(orderGateway).saveOrder(order);
        inOrder.verify(cartGateway).clearCart(userId);
    }

    @Test
    void pickupHasNoFeeAndNoAddress() {
        when(cartGateway.getItems(userId)).thenReturn(prdCart);

        var order = orders.placeOrder(userId, FulfillmentType.PICKUP, "ignored");

        assertThat(order.getTotal()).isEqualTo(new BigDecimal("12.88"));
        assertThat(order.getAddress()).isNull();
    }

    @Test
    void emptyCartOrMissingDeliveryAddressIsInvalid() {
        when(cartGateway.getItems(userId)).thenReturn(List.of());
        assertThatThrownBy(() -> orders.placeOrder(userId, FulfillmentType.PICKUP, null))
                .isInstanceOf(InvalidDataException.class);

        when(cartGateway.getItems(userId)).thenReturn(prdCart);
        assertThatThrownBy(() -> orders.placeOrder(userId, FulfillmentType.DELIVERY, " "))
                .isInstanceOf(InvalidDataException.class);

        verify(orderGateway, never()).saveOrder(any());
        verify(cartGateway, never()).clearCart(any());
    }

    @Test
    void otherUsersOrderIsNotFoundButAdminSeesIt() {
        var order = pendingOrder();
        when(orderGateway.findOrder(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orders.getOrder(UUID.randomUUID(), false, order.getId()))
                .isInstanceOf(OrderNotFoundException.class);
        assertThat(orders.getOrder(UUID.randomUUID(), true, order.getId())).isSameAs(order);
        assertThat(orders.getOrder(userId, false, order.getId())).isSameAs(order);
    }

    @Test
    void ownerCancelsWhilePendingOrConfirmed() {
        var order = pendingOrder();
        when(orderGateway.findOrder(order.getId())).thenReturn(Optional.of(order));

        orders.cancelOrder(userId, false, order.getId());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderGateway).saveOrder(order);
    }

    @Test
    void cancellingAReadyOrderConflicts() {
        var order = pendingOrder();
        order.moveTo(OrderStatus.CONFIRMED);
        order.moveTo(OrderStatus.READY);
        when(orderGateway.findOrder(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orders.cancelOrder(userId, false, order.getId()))
                .isInstanceOf(OrderStatusConflictException.class);
        verify(orderGateway, never()).saveOrder(any());
    }

    @Test
    void invalidStatusChangeConflicts() {
        var order = pendingOrder();
        when(orderGateway.findOrder(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orders.updateStatus(order.getId(), OrderStatus.COMPLETED))
                .isInstanceOf(OrderStatusConflictException.class);
        assertThat(orders.updateStatus(order.getId(), OrderStatus.CONFIRMED).getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void listingsArePaged() {
        assertThatThrownBy(() -> orders.listOwnOrders(userId, 0, 51)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> orders.listAllOrders(-1, 10)).isInstanceOf(InvalidDataException.class);
    }

    // the id is generated by JPA; a mock lookup by null id is enough for these tests
    private Order pendingOrder() {
        return new Order(userId, FulfillmentType.PICKUP, null, prdCart, PriceSummary.of(prdCart, BigDecimal.ZERO));
    }
}
