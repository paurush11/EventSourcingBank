package dev.codescreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizationResponse {
    private String userId;
    private String messageId;
    private ResponseCode responseCode;
    private Amount balance;
}
