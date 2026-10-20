package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByName(String name);

    // units sold in COMPLETED orders; unsold products fill the rest by name (RF-CAT-6)
    @Query(value = """
            SELECT p.*
            FROM product p
            LEFT JOIN (
                SELECT oi.product_id, SUM(oi.quantity) AS sold
                FROM order_item oi
                INNER JOIN orders o ON o.id = oi.order_id
                WHERE o.status = 'COMPLETED'
                GROUP BY oi.product_id
            ) s ON s.product_id = p.id
            ORDER BY COALESCE(s.sold, 0) DESC, p.name
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> findBestSellers(@Param("limit") int limit);
}
