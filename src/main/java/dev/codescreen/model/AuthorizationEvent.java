package dev.codescreen.model;

import dev.codescreen.dto.Amount;
import dev.codescreen.dto.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthorizationEvent extends Event {
    private boolean approved;

    public AuthorizationEvent(String messageId, String userId, LocalDateTime timestamp, String eventType, Amount amount, ResponseCode responseCode, boolean approved) {
        super(messageId,userId, timestamp, eventType, amount, responseCode);
        this.approved = approved;
    }
}