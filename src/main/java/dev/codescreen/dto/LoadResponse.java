package dev.codescreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoadResponse {
    private String userId;
    private String messageId;
    private Amount balance;
}
