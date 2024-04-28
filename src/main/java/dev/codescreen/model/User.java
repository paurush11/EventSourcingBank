package dev.codescreen.model;

import dev.codescreen.dto.Amount;
import dev.codescreen.dto.UserBalance;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;


@Data
@NoArgsConstructor
@Entity
@RedisHash("User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // Assuming you're using a database with auto-generated IDs
    private String username;
    private String email;
    private String password; // You should encrypt and store passwords securely
    private Amount balance; // Assuming balance is in double for simplicity
    // Add other attributes as needed (e.g., roles)

    // Constructors, getters, setters, and other methods
}
