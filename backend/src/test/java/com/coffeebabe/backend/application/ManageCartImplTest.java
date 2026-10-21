package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.CartItem;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.exceptions.ProductNotFoundException;
import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManageCartImplTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private CartCrudGateway cartGateway;
    private ProductCrudGateway productGateway;
    private ManageCartImpl cart;

    @BeforeEach
    void setUp() {
        cartGateway = mock(CartCrudGateway.class);
        productGateway = mock(ProductCrudGateway.class);
        cart = new ManageCartImpl(cartGateway, productGateway);
    }

    @Test
    void addingNewProductCreatesItemWithQuantityOne() {
        when(cartGateway.findItem(userId, productId)).thenReturn(Optional.empty());

        cart.addToCart(userId, productId);

        var saved = ArgumentCaptor.forClass(Cart.class);
        verify(cartGateway).saveItem(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(userId);
        assertThat(saved.getValue().getQuantity()).isEqualTo(1);
    }

    @Test
    void addingSameProductIncrementsQuantity() {
        var item = new Cart(userId, productId);
        when(cartGateway.findItem(userId, productId)).thenReturn(Optional.of(item));

        cart.addToCart(userId, productId);

        verify(cartGateway).saveItem(item);
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    void addingUnknownProductIsNotFound() {
        when(productGateway.getProductById(productId)).thenThrow(new ProductNotFoundException("x"));

        assertThatThrownBy(() -> cart.addToCart(userId, productId)).isInstanceOf(ProductNotFoundException.class);
        verify(cartGateway, never()).saveItem(any());
    }

    @Test
    void decreasingLastUnitRemovesItem() {
        var item = new Cart(userId, productId);
        when(cartGateway.findItem(userId, productId)).thenReturn(Optional.of(item));

        cart.decreaseItemQuantity(userId, productId);

        verify(cartGateway).deleteItem(item);
        verify(cartGateway, never()).saveItem(any());
    }

    @Test
    void decreasingKeepsItemWhileQuantityAboveOne() {
        var item = new Cart(userId, productId);
        item.increase();
        when(cartGateway.findItem(userId, productId)).thenReturn(Optional.of(item));

        cart.decreaseItemQuantity(userId, productId);

        verify(cartGateway).saveItem(item);
        assertThat(item.getQuantity()).isEqualTo(1);
    }

    @Test
    void cartHasTotalsWithoutDeliveryFee() {
        var latte = new Product("Latte", "d", new BigDecimal("4.00"), ProductCategory.HOT_COFFEE);
        when(cartGateway.getItems(userId)).thenReturn(List.of(new CartItem(latte, 2)));

        var summary = cart.getCart(userId);

        assertThat(summary.prices().subtotal()).isEqualTo(new BigDecimal("8.00"));
        assertThat(summary.prices().tax()).isEqualTo(new BigDecimal("0.24"));
        assertThat(summary.prices().total()).isEqualTo(new BigDecimal("8.24"));
    }
}
