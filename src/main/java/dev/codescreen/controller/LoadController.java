package dev.codescreen.controller;

import dev.codescreen.dto.*;
import dev.codescreen.model.LoadEvent;
import dev.codescreen.model.User;
import dev.codescreen.repository.EventRepository;
import dev.codescreen.repository.UserRepository;
import dev.codescreen.service.BalanceProjectionService;
import dev.codescreen.service.EventService;
import dev.codescreen.service.TransactionCommandService;
import dev.codescreen.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/load" )
public class LoadController {

    @Autowired
    private TransactionCommandService transactionCommandService;
    @Autowired
    private BalanceProjectionService balanceProjectionService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;

    private LoadEvent createLoadEvent(LoadRequest request) {
        BigDecimal transactionAmount = new BigDecimal(request.getTransactionAmount().getAmount());
        LoadEvent event =  new LoadEvent(
                request.getMessageId(),
                request.getUserId(),
                LocalDateTime.now(),
                "LOAD",
                request.getTransactionAmount(),
                null,  // Response code will be set in the service
                false  // approved status will be determined in the service
        );
        eventService.saveEvent(event);
        return event;
    }

    @PutMapping("/{messageId}")
    public DeferredResult<ResponseEntity<?>> loadFunds(@PathVariable("messageId") String messageId, @RequestBody LoadRequest request) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        LoadEvent event = createLoadEvent(request);
        User user = userService.getUserById(event.getUserId());
        try{
            transactionCommandService.sendEvent(event, "loadQueue").thenAccept(aVoid->{
                BigDecimal newBalance = balanceProjectionService.getCurrentBalance(request.getUserId());
                Amount newAmount = new Amount(newBalance.toPlainString(), request.getTransactionAmount().getCurrency(), request.getTransactionAmount().getDebitOrCredit());
                deferredResult.setResult(new ResponseEntity<>(new LoadResponse(event.getUserId(), messageId, newAmount), HttpStatus.CREATED));
                event.setResponseCode(ResponseCode.APPROVED);
                event.setSuccess(true);
                eventService.saveEvent(event);
                user.setBalance(newAmount);
                userService.updateUser(user);
            }).exceptionally(ex->{
                event.setResponseCode(ResponseCode.DECLINED);
                event.setSuccess(false);
                eventService.saveEvent(event);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            });

        }catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such User found");
        }

        return deferredResult;
    }
}
