package com.gvp.financialdashboard.repository;

import com.gvp.financialdashboard.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByClerkId(String clerkId);
    java.util.Optional<User> findByClerkId(String clerkId);
}
