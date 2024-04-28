package dev.codescreen.listener;

import dev.codescreen.model.AuthorizationEvent;
import dev.codescreen.model.LoadEvent;
import dev.codescreen.repository.EventRepository;
import dev.codescreen.service.BalanceProjectionService;
import dev.codescreen.service.CardTypeDebitOrCredit;
import dev.codescreen.service.TransactionCommandService;
import dev.codescreen.service.UpdateOperationType;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class BalanceUpdateListener {
    @Autowired
    private BalanceProjectionService balanceProjectionService;

    @Autowired
    private TransactionCommandService transactionCommandService;

    @RabbitListener(queues = "authQueue")
    public void processAuthorizationEvent(AuthorizationEvent event) {
        // Assuming positive amounts represent credits and negative amounts represent debits
        try{
            BigDecimal amt = new BigDecimal(event.getAmount().getAmount());
            if(event.getAmount().getDebitOrCredit().equalsIgnoreCase("debit")){
                balanceProjectionService.updateBalance(event.getUserId(), amt, UpdateOperationType.SUBTRACT, CardTypeDebitOrCredit.DEBIT);
            }else{
                balanceProjectionService.updateBalance(event.getUserId(), amt, UpdateOperationType.SUBTRACT, CardTypeDebitOrCredit.CREDIT);
            }
            transactionCommandService.completeEvent(event.getId());
        }catch (Exception e){
            transactionCommandService.completeEventWithException(event.getId(), e);
        }
    }

    @RabbitListener(queues = "loadQueue")
    public void processLoadEvent(LoadEvent event) {
        System.out.println(event);
        try{
            BigDecimal amt = new BigDecimal(event.getAmount().getAmount());
            if(event.getAmount().getDebitOrCredit().equalsIgnoreCase("debit")){
                balanceProjectionService.updateBalance(event.getUserId(), amt, UpdateOperationType.ADD, CardTypeDebitOrCredit.DEBIT);
            }else{
                balanceProjectionService.updateBalance(event.getUserId(), amt, UpdateOperationType.ADD, CardTypeDebitOrCredit.CREDIT);
            }
            transactionCommandService.completeEvent(event.getId());
        }catch(Exception e){
            transactionCommandService.completeEventWithException(event.getId(), e);
        }
    }
}
