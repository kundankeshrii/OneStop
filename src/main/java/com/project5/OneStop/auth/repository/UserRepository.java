package com.project5.OneStop.auth.repository;

import com.project5.OneStop.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(
            com.project5.OneStop.auth.entity.AuthProvider provider,
            String providerId
    );

    boolean existsByEmail(String email);
}