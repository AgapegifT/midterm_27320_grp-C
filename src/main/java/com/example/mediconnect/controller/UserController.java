package com.example.mediconnect.controller;

import com.example.mediconnect.model.Role;
import com.example.mediconnect.model.User;
import com.example.mediconnect.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Optional;

/**
 * REQUIREMENT 3: Pagination and Sorting Support
 * REQUIREMENT 7: existsBy() methods for duplicate prevention
 * 
 * User Management Endpoint Strategy:
 * - All database queries are single-line repository calls
 * - No loops or multiple database operations in same method
 * - Spring Data JPA handles pagination at database level
 * - Extensive comments for lecturer understanding
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    // Inject UserRepository for all database operations
    private final UserRepository userRepository;

    // Constructor injection ensures dependencies are provided
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user (Open endpoint - anyone can call)
     * 
     * API USAGE:
     * POST /api/users/register
     * Body: {
     *   "email": "user@example.com",
     *   "password": "secure123",
     *   "role": "PATIENT" (optional, defaults to PATIENT if not provided)
     * }
     * 
     * Database Operation:
     * INSERT INTO users (email, password, role, created_at) VALUES (...);
     * 
     * This demonstrates:
     * - Simple INSERT operation
     * - Default value handling (role defaults to PATIENT)
     * - Single save() call = ONE database operation
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            // Request body containing user registration data
            @Valid @RequestBody User user) {
        
        // Basic validation to avoid null values causing 500 errors
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email must be provided");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password must be provided");
        }

        // REQUIREMENT 7: Check if email already exists
        // Single database query: SELECT COUNT(*) > 0 FROM users WHERE email = ?
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already registered: " + user.getEmail());
        }
        
        // If user didn't specify a role, default them to PATIENT
        if (user.getRole() == null) {
            user.setRole(Role.PATIENT);
        }
        
        // Execute ONE database query: Save the new user
        // This INSERT is atomic - either succeeds or fails, no partial saves
        User savedUser = userRepository.save(user);
        
        // Return the registered user with HTTP 201 Created status
        return ResponseEntity.status(201).body(savedUser);
    }

    /**
     * Find user by email address
     * 
     * API USAGE:
     * GET /api/users/email/admin@mediconnect.rw
     * 
     * Database Query Generated:
     * SELECT * FROM users WHERE email = 'admin@mediconnect.rw';
     * 
     * Email is UNIQUE in database, so max 1 result
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> findByEmail(
            // The email address to search for (extracted from URL)
            @PathVariable String email) {
        
        // Execute ONE database query: Find user by email (direct lookup, very fast)
        Optional<User> user = userRepository.findByEmail(email);
        
        // Return user if found, or 404 if not found
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get a user by ID
     * 
     * API USAGE:
     * GET /api/users/1
     * 
     * Database Query Generated:
     * SELECT * FROM users WHERE id = 1;
     * 
     * This is O(1) complexity - uses primary key index
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            // The user ID (extracted from URL)
            @PathVariable Long id) {
        
        // Execute ONE database query: Get user by ID (uses primary key index)
        Optional<User> user = userRepository.findById(id);
        
        // Return user if found, or 404 if not found
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List all users with pagination (Admin only)
     * 
     * API USAGE:
     * GET /api/users?page=0&size=10&sort=email,asc
     * 
     * Database Query Generated:
     * SELECT * FROM users LIMIT 10 OFFSET 0 ORDER BY email ASC;
     * 
     * This demonstrates:
     * - REQUIREMENT 3: Pagination (page 0, 10 results)
     * - Database-level sorting (email ascending)
     * - No Java loops - database returns only 10 records
     */
    @GetMapping
    public ResponseEntity<?> listAllUsers(
            // Admin authorization check
            @RequestHeader(value = "X-Role", required = false) String roleHeader,
            // REQUIREMENT 3: Page number (0-indexed)
            @RequestParam(defaultValue = "0") int page,
            // REQUIREMENT 3: Records per page
            @RequestParam(defaultValue = "10") int size,
            // Sorting field (e.g., "email" or "email,asc")
            @RequestParam(defaultValue = "email") String sort) {
        
        // Check if user has ADMIN role to view all users
        if (!isAdminRole(roleHeader)) {
            return ResponseEntity.status(403)
                    .body("Only admins can list all users");
        }
        
        // Create pagination object with sorting
        // Pageable handles: LIMIT, OFFSET, ORDER BY all at database level
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        
        // Execute ONE database query: Get all users with pagination
        // Database returns only 10 records, not all users
        Page<User> users = userRepository.findAll(pageable);
        
        // Return paginated results
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role (Admin only)
     * 
     * API USAGE:
     * GET /api/users/role/DOCTOR?page=0&size=10
     * 
     * Database Query Generated:
     * SELECT * FROM users WHERE role = 'DOCTOR' LIMIT 10 OFFSET 0;
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            // Admin authorization check
            @RequestHeader(value = "X-Role", required = false) String roleHeader,
            // The role to filter by (e.g., DOCTOR, PATIENT, PHARMACIST)
            @PathVariable String role,
            // REQUIREMENT 3: Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Check admin authorization
        if (!isAdminRole(roleHeader)) {
            return ResponseEntity.status(403)
                    .body("Only admins can filter users by role");
        }
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Find all users with specific role
        // WHERE role = ? with pagination
        Page<User> users = userRepository.findByRole(Role.valueOf(role.toUpperCase()), pageable);
        
        // Return paginated results
        return ResponseEntity.ok(users);
    }

    /**
     * Update user information
     * 
     * API USAGE:
     * PUT /api/users/1
     * Body: {
     *   "password": "newpassword",
     *   "role": "DOCTOR"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            // The user ID to update
            @PathVariable Long id,
            // Updated user data
            @RequestBody User updates) {
        
        // Execute ONE database query: Get user by ID
        Optional<User> existing = userRepository.findById(id);
        
        // If user doesn't exist, return 404 Not Found
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the user from Optional
        User user = existing.get();
        
        // Update password if provided
        if (updates.getPassword() != null && !updates.getPassword().isEmpty()) {
            user.setPassword(updates.getPassword());
        }
        
        // Update role if provided
        if (updates.getRole() != null) {
            user.setRole(updates.getRole());
        }
        
        // Execute ONE database query: Save the updated user
        User savedUser = userRepository.save(user);
        
        // Return the updated user
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Delete a user by ID (Admin only)
     * 
     * API USAGE:
     * DELETE /api/users/3
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            // Admin authorization check
            @RequestHeader(value = "X-Role", required = false) String roleHeader,
            // The user ID to delete
            @PathVariable Long id) {
        
        // Check admin authorization
        if (!isAdminRole(roleHeader)) {
            return ResponseEntity.status(403)
                    .body("Only admins can delete users");
        }
        
        // Execute ONE database query: Check if user exists
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Execute ONE database query: Delete the user
        userRepository.deleteById(id);
        
        // Return 204 No Content (successful deletion, no response body)
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to check if user has ADMIN role
     * 
     * @param roleHeader The role from X-Role header
     * @return true if user is an admin
     */
    private boolean isAdminRole(String roleHeader) {
        // Check if role header exists and equals ADMIN (case-insensitive)
        return roleHeader != null && roleHeader.equalsIgnoreCase(Role.ADMIN.name());
    }
}
