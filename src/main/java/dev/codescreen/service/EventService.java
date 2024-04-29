package dev.codescreen.service;

import dev.codescreen.dto.Amount;
import dev.codescreen.dto.ResponseCode;
import dev.codescreen.model.Event;
import dev.codescreen.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BalanceProjectionService balanceProjectionService;

    @Autowired
    private FetchCurrencyAndConvertService fetchCurrencyAndConvertService;

    @Transactional(readOnly = true)
    public List<Event> replayEvents(String UserId) {
        return eventRepository.findAllByOrderByTimestampAsc().stream().filter(event -> event.getUserId().equals(UserId)).collect(Collectors.toList());
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    public Amount replayEventsAndGetNewBalance(String userId) {
        Amount finalBalance =  eventRepository.findAllByOrderByTimestampAsc().stream()
                .filter(event -> event.getUserId().equals(userId) && event.getResponseCode().equals(ResponseCode.APPROVED))
                .map(event -> {
                    BigDecimal amountValue = new BigDecimal(event.getAmount().getAmount());
                    BigDecimal factor = fetchCurrencyAndConvertService.getConversionFactor(event.getAmount().getCurrency());
                    System.out.println(factor);
                    if(event.getEventType().equalsIgnoreCase("LOAD")){
                        return new Amount(amountValue.divide(factor,3, RoundingMode.DOWN).toString(), event.getAmount().getCurrency(), event.getAmount().getDebitOrCredit());
                    }else{
                        return new Amount(amountValue.divide(factor,3, RoundingMode.DOWN).negate().toString(), event.getAmount().getCurrency(), event.getAmount().getDebitOrCredit());
                    }
                })
                .reduce(new Amount("0", "USD", "credit"), (a,b)->{
                    BigDecimal total = new BigDecimal(a.getAmount()).add(new BigDecimal(b.getAmount()));
                    return new Amount(total.toString(), a.getCurrency(), total.compareTo(BigDecimal.ZERO) >= 0 ? "credit" : "debit");
                });
        balanceProjectionService.setBalance(userId,new BigDecimal(finalBalance.getAmount()));
        return finalBalance;
    }


    private String determineRoutingKey(Event event) {
        return event.getEventType().equals("LOAD") ? "loadQueue" : "authQueue";
    }
}
