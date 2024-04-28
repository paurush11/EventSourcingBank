package dev.codescreen.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String email;
    private String password; // You should encrypt and store passwords securely
    private Amount balance;
}
