package com.example.demo.DB;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String password, String email) {
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);

        return userRepository.save(newUser);
    }

    public boolean checkUser(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }
        return userRepository.existsByUsernameAndPassword(username, password);
    }
}
