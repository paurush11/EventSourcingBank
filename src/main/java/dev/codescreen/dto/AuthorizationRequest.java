package dev.codescreen.dto;

import lombok.Data;

@Data
public class AuthorizationRequest {
    private String userId;
    private String messageId;
    private Amount transactionAmount;
}
