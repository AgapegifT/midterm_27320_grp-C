package com.example.mediconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REQUIREMENT 6: User entity - ONE-TO-ONE relationship with Profile
 * 
 * RELATIONSHIP EXPLANATION:
 * - User table stores authentication credentials (email, password)
 * - Profile table stores user details (firstName, lastName, phone, etc.)
 * - Each User has exactly ONE Profile
 * - The Profile entity owns the relationship (has the @JoinColumn with user_id foreign key)
 * 
 * Database Schema:
 * users table: id, email, password_hash, role
 * profiles table: id, user_id (FK to users), firstName, lastName, phone, etc.
 * 
 * This separation provides:
 * - Better security (authentication separate from personal data)
 * - Flexibility for role-specific fields
 * - Clean separation of concerns
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    /**
     * ONE-TO-ONE: Each user has exactly one profile
     * This is the inverse side of the relationship (Profile owns it via @JoinColumn)
     * The mappedBy tells JPA that Profile.user is the owner
     * cascade=ALL means if we delete a user, its profile is also deleted
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    // Constructors
    public User() {}

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public Profile getProfile() {
        return profile;
    }
    
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
