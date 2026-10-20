package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query(value = """
            SELECT p.id, p.name, p.image_url, p.description, p.price
            FROM best_seller b
            INNER JOIN product p
            ON b.product_id = p.id
            """, nativeQuery = true)
    List<Product> findAllBestSellers();
}
