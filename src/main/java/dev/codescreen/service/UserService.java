package dev.codescreen.service;

import dev.codescreen.exception.UserNotFoundException;
import dev.codescreen.model.User;
import dev.codescreen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
//
    public User createUser(User user) {
        return userRepository.save(user); // Handles saving a new user
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(User user) {
        return userRepository.save(user); // Assumes user exists and updates
    }
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
