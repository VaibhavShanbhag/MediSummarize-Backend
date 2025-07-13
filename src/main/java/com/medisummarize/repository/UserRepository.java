package com.medisummarize.repository;

import com.medisummarize.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findById(Long id);
    boolean existsByEmail(String email);
}
