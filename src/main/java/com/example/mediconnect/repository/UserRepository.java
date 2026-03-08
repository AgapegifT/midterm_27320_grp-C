package com.example.mediconnect.repository;

import com.example.mediconnect.model.Role;
import com.example.mediconnect.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REQUIREMENT 7: existsBy() Method
 * REQUIREMENT 3: Pagination and Sorting
 * 
 * Spring Data JPA Repository Interface for User entity
 * 
 * HOW IT WORKS:
 * Method names following the convention are automatically implemented by Spring
 * Example: existsByEmail(String email) generates SQL:
 * SELECT COUNT(u) > 0 FROM users u WHERE u.email = ?1
 * 
 * No need to write SQL! Spring parses the method name and creates the query!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * REQUIREMENT 7: Check if user with email already exists
     * 
     * HOW IT WORKS:
     * Spring generates: SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 FROM users WHERE email = ?
     * 
     * USE CASE:
     * Before allowing registration, check if email already taken:
     * 
     * if (userRepository.existsByEmail(email)) {
     *     throw new RuntimeException("Email already registered!");
     * }
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by email for login authentication
     * 
     * SQL Generated: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);
    
    /**
     * REQUIREMENT 3: Find users by role with pagination
     * 
     * SQL Generated: SELECT * FROM users WHERE role = ? LIMIT ? OFFSET ?
     * 
     * Useful for admin screens to list all doctors, pharmacists, or patients
     */
    Page<User> findByRole(Role role, Pageable pageable);
    
    /**
     * Custom query to find user with their profile information
     * 
     * Loads both user and profile in single query (LEFT JOIN FETCH)
     * Prevents N+1 problem
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = ?1")
    Optional<User> findByIdWithProfile(Long id);
}
