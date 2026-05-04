package com.coffeebabe.backend.infra.persistence.jpa;

import com.coffeebabe.backend.core.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
