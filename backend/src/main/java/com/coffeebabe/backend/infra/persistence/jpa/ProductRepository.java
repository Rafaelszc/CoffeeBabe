package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByName(String name);

    @Query(value = """
            SELECT p.*
            FROM best_seller b
            INNER JOIN product p
            ON b.product_id = p.id
            """, nativeQuery = true)
    List<Product> findAllBestSellers();
}
