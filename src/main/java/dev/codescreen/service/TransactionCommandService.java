package dev.codescreen.service;


import dev.codescreen.model.Event;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class TransactionCommandService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private final String exchangeName = "transaction-exchange";
    private final ConcurrentHashMap<String, CompletableFuture<Void>> completionMap = new ConcurrentHashMap<>();


    public CompletableFuture<Void> sendEvent(Event event, String routingKey) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completionMap.put(event.getId(), completableFuture);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
        return completableFuture;
    }

    public void completeEvent(String messageId) {
        CompletableFuture<Void> future = completionMap.remove(messageId);
        if (future != null) {
            future.complete(null);
        }
    }
    public void completeEventWithException(String messageId, Exception exception) {
        CompletableFuture<Void> future = completionMap.remove(messageId);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }

}
