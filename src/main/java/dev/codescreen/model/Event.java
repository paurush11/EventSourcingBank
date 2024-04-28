package dev.codescreen.model;

import dev.codescreen.dto.Amount;
import dev.codescreen.dto.ResponseCode;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Event")
public class Event implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String messageId;

    public Event(String messageId, String userId, LocalDateTime timestamp, String eventType, Amount amount, ResponseCode responseCode) {
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.amount = amount;
        this.responseCode = responseCode;
    }

    private LocalDateTime timestamp;
    private String eventType;
    private Amount amount;
    private ResponseCode responseCode;

}
