package com.example.mediconnect;

import com.example.mediconnect.model.Role;
import com.example.mediconnect.model.User;
import com.example.mediconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Simple runner that adds an admin user on startup for convenience.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepo;

    public DataInitializer(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.findByEmail("admin@mediconnect.rw").isEmpty()) {
            userRepo.save(new User("admin@mediconnect.rw", "admin", Role.ADMIN));
        }
    }
}
