package com.example.grpcuser.repository;

import com.example.grpcuser.domain.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByExternalId(String externalId);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "cars")
    Optional<UserEntity> findWithCarsById(UUID id);
}
