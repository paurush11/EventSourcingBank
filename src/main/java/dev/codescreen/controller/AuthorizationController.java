package dev.codescreen.controller;

import dev.codescreen.dto.*;
import dev.codescreen.model.AuthorizationEvent;
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

@RestController
@RequestMapping(value = "/authorization")
public class AuthorizationController {

    @Autowired
    private TransactionCommandService transactionCommandService;
    @Autowired
    private BalanceProjectionService balanceProjectionService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;


    private AuthorizationEvent createAuthorizationEvent(AuthorizationRequest request) {
        BigDecimal transactionAmount = new BigDecimal(request.getTransactionAmount().getAmount());
        AuthorizationEvent event = new AuthorizationEvent(
                request.getMessageId(),
                request.getUserId(),
                LocalDateTime.now(),
                "AUTHORIZATION",
                request.getTransactionAmount(),
                null,  // Response code will be set in the service
                false  // approved status will be determined in the service
        );
        eventService.saveEvent(event);
        return event;
    }

    @PutMapping("/{messageId}")
    public DeferredResult<ResponseEntity<?>> authorizeFunds(@PathVariable("messageId") String messageId, @RequestBody AuthorizationRequest request) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        AuthorizationEvent event = createAuthorizationEvent(request);
        User user = userService.getUserById(event.getUserId());
        try{
            BigDecimal oldBalance = balanceProjectionService.getCurrentBalance(request.getUserId());
            transactionCommandService.sendEvent(event, "authQueue").thenAccept(vVoid -> {
                BigDecimal newBalance = balanceProjectionService.getCurrentBalance(request.getUserId());
                Amount newAmount = new Amount(newBalance.toPlainString(), request.getTransactionAmount().getCurrency(), request.getTransactionAmount().getDebitOrCredit());
                if (oldBalance.equals(newBalance)) {
                    event.setResponseCode(ResponseCode.DECLINED);
                    event.setApproved(false);
                    deferredResult.setResult(new ResponseEntity<>(new AuthorizationResponse(event.getUserId(), messageId, event.getResponseCode(), newAmount), HttpStatus.FORBIDDEN));
                } else {
                    event.setResponseCode(ResponseCode.APPROVED);
                    event.setApproved(true);
                    deferredResult.setResult(new ResponseEntity<>(new AuthorizationResponse(event.getUserId(), messageId, event.getResponseCode(), newAmount), HttpStatus.ACCEPTED));

                }
                eventService.saveEvent(event);
                UserBalance balance = new UserBalance(newAmount.getAmount(), newAmount.getCurrency());
                user.setBalance(balance);
                userService.updateUser(user);
            }).exceptionally(ex -> {
                eventService.saveEvent(event);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            });

        }catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such User found");
        }



        return deferredResult;
    }
}
