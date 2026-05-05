package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Cart;
import com.coffeebabe.backend.core.entities.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    @Query(value = """
            SELECT p.id, p.name, p.image_url, p.description, p.price
            FROM cart c
            INNER JOIN products p
            ON c.product_id = p.id
            WHERE c.user_id = :userId
            """, nativeQuery = true)
    List<Product> findAllProductsFromUserId(@Param("userId") UUID userId);

    @Transactional
    void deleteByProductIdAndUserId(UUID productId, UUID userId);

    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM cart
            WHERE id IN (
                SELECT id
                FROM cart
                LIMIT 1
            )""", nativeQuery = true)
    void deleteFirstByProductIdAndUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);
}
