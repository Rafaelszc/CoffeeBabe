package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserIdAndProductId(UUID userId, UUID productId);

    List<Cart> findByUserId(UUID userId);

    @Transactional
    void deleteByUserId(UUID userId);
}
