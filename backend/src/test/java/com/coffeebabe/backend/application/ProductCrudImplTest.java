package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;
import com.coffeebabe.backend.core.exceptions.InvalidDataException;
import com.coffeebabe.backend.core.exceptions.ProductAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.ProductNotFoundException;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductCrudImplTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1};
    private static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);

    private ProductCrudGateway gateway;
    private ProductCrudImpl products;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        gateway = mock(ProductCrudGateway.class);
        products = new ProductCrudImpl(gateway);
    }

    @Test
    void searchTrimsQueryAndTreatsNullAsEmpty() {
        var page = new PageResult<Product>(List.of(), 0, 12, 0, 0);
        when(gateway.searchProducts(any(), any(), anyInt(), anyInt())).thenReturn(page);

        assertThat(products.searchProducts("  cap ", ProductCategory.HOT_COFFEE, 0, 12)).isSameAs(page);
        products.searchProducts(null, null, 1, 50);

        verify(gateway).searchProducts("cap", ProductCategory.HOT_COFFEE, 0, 12);
        verify(gateway).searchProducts("", null, 1, 50);
    }

    @Test
    void searchRejectsInvalidPaging() {
        assertThatThrownBy(() -> products.searchProducts("", null, 0, 51)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> products.searchProducts("", null, 0, 0)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> products.searchProducts("", null, -1, 12)).isInstanceOf(InvalidDataException.class);
        verifyNoInteractions(gateway);
    }

    @Test
    void bestSellersAreTheTopSix() {
        products.getBestSellers();

        verify(gateway).getBestSellers(6);
    }

    @Test
    void priceIsStoredWithTwoDecimalsHalfUp() {
        var product = new Product("Latte", "d", new BigDecimal("4.005"), ProductCategory.HOT_COFFEE);
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("4.01"));
    }

    @Test
    void duplicateNameIsReported() {
        doThrow(new DataIntegrityViolationException("name")).when(gateway).saveProduct(any());

        assertThatThrownBy(() -> products.publishProduct(new Product("Mocha", "d", BigDecimal.ONE, ProductCategory.HOT_COFFEE)))
                .isInstanceOf(ProductAlreadyExistsException.class);
    }

    @Test
    void updateChangesExistingProduct() {
        var product = new Product("Mocha", "d", BigDecimal.ONE, ProductCategory.HOT_COFFEE);
        when(gateway.getProductById(id)).thenReturn(product);

        products.updateProduct(id, "Iced Mocha", "gelado", new BigDecimal("6.5"), ProductCategory.COLD_COFFEE);

        verify(gateway).saveProduct(product);
        assertThat(product.getName()).isEqualTo("Iced Mocha");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("6.50"));
        assertThat(product.getCategory()).isEqualTo(ProductCategory.COLD_COFFEE);
    }

    @Test
    void removingUnknownProductIsNotFound() {
        when(gateway.getProductById(id)).thenThrow(new ProductNotFoundException("x"));

        assertThatThrownBy(() -> products.removeProduct(id)).isInstanceOf(ProductNotFoundException.class);
        verify(gateway, never()).deleteProductById(any());
    }

    @Test
    void uploadDetectsTypeBySignature() {
        assertThat(uploadedType(PNG)).isEqualTo("image/png");
        assertThat(uploadedType(JPEG)).isEqualTo("image/jpeg");
        assertThat(uploadedType(WEBP)).isEqualTo("image/webp");
    }

    @Test
    void uploadStoresSha256AsEtag() {
        products.uploadImage(id, PNG);

        var saved = ArgumentCaptor.forClass(ProductImage.class);
        verify(gateway).saveImage(saved.capture());
        assertThat(saved.getValue().getEtag()).hasSize(64).matches("[0-9a-f]+");
        assertThat(saved.getValue().getData()).isEqualTo(PNG);
    }

    @Test
    void uploadRejectsFakeImagesSvgAndEmptyFiles() {
        var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script/></svg>".getBytes(StandardCharsets.UTF_8);
        var pngNamedText = "just text in a .png file".getBytes(StandardCharsets.UTF_8);

        for (var data : List.of(svg, pngNamedText, new byte[0])) {
            assertThatThrownBy(() -> products.uploadImage(id, data)).isInstanceOf(InvalidDataException.class);
        }
        verify(gateway, never()).saveImage(any());
    }

    @Test
    void uploadRejectsFilesOver2Mb() {
        var big = Arrays.copyOf(PNG, ProductCrudImpl.MAX_IMAGE_BYTES + 1);

        assertThatThrownBy(() -> products.uploadImage(id, big)).isInstanceOf(InvalidDataException.class);
        verify(gateway, never()).saveImage(any());
    }

    @Test
    void uploadToUnknownProductIsNotFound() {
        when(gateway.getProductById(id)).thenThrow(new ProductNotFoundException("x"));

        assertThatThrownBy(() -> products.uploadImage(id, PNG)).isInstanceOf(ProductNotFoundException.class);
    }

    private String uploadedType(byte[] data) {
        clearInvocations(gateway);
        products.uploadImage(id, data);
        var saved = ArgumentCaptor.forClass(ProductImage.class);
        verify(gateway).saveImage(saved.capture());
        return saved.getValue().getContentType();
    }
}
