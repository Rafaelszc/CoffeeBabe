package com.coffeebabe.backend.application;

import com.coffeebabe.backend.core.entities.PageResult;
import com.coffeebabe.backend.core.entities.Product;
import com.coffeebabe.backend.core.entities.ProductCategory;
import com.coffeebabe.backend.core.entities.ProductImage;
import com.coffeebabe.backend.core.exceptions.InvalidDataException;
import com.coffeebabe.backend.core.exceptions.ProductAlreadyExistsException;
import com.coffeebabe.backend.core.exceptions.ProductNotFoundException;
import com.coffeebabe.backend.core.gateways.ProductCrudGateway;
import com.coffeebabe.backend.core.usecases.ProductCrudUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductCrudImpl implements ProductCrudUseCase {
    public static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private final ProductCrudGateway productCrudGateway;

    public ProductCrudImpl(ProductCrudGateway productCrudGateway) {
        this.productCrudGateway = productCrudGateway;
    }

    @Override
    public PageResult<Product> searchProducts(String query, ProductCategory category, int page, int size) {
        PageResult.checkPaging(page, size);
        return productCrudGateway.searchProducts(query == null ? "" : query.trim(), category, page, size);
    }

    @Override
    public List<Product> getBestSellers() {
        return productCrudGateway.getBestSellers();
    }

    @Override
    public Product getProductById(UUID id) {
        return productCrudGateway.getProductById(id);
    }

    @Override
    public void publishProduct(Product product) {
        save(product);
    }

    @Override
    public void updateProduct(UUID id, String name, String description, BigDecimal price, ProductCategory category) {
        var product = productCrudGateway.getProductById(id);
        product.update(name, description, price, category);
        save(product);
    }

    @Override
    public void removeProduct(UUID productId) {
        // throws ProductNotFoundException; the image goes with the product (ON DELETE CASCADE)
        productCrudGateway.getProductById(productId);
        productCrudGateway.deleteProductById(productId);
    }

    @Override
    public void uploadImage(UUID productId, byte[] data) {
        productCrudGateway.getProductById(productId);

        if (data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            throw new InvalidDataException("InvalidDataException");
        }
        var contentType = detectContentType(data)
                .orElseThrow(() -> new InvalidDataException("InvalidDataException"));

        productCrudGateway.saveImage(new ProductImage(productId, contentType, data, sha256(data)));
    }

    @Override
    public ProductImage getImage(UUID productId) {
        return productCrudGateway.findImage(productId)
                .orElseThrow(() -> new ProductNotFoundException("ProductNotFoundException"));
    }

    private void save(Product product) {
        try {
            productCrudGateway.saveProduct(product);
        } catch (DataIntegrityViolationException e) {
            throw new ProductAlreadyExistsException("ProductAlreadyExistsException");
        }
    }

    // by content signature, never by file name or the client's Content-Type (SEC-13)
    static Optional<String> detectContentType(byte[] data) {
        if (startsWith(data, 0, JPEG)) return Optional.of("image/jpeg");
        if (startsWith(data, 0, PNG)) return Optional.of("image/png");
        if (startsWith(data, 0, RIFF) && startsWith(data, 8, WEBP)) return Optional.of("image/webp");
        return Optional.empty();
    }

    private static boolean startsWith(byte[] data, int offset, byte[] prefix) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
