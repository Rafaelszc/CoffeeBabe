package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}
