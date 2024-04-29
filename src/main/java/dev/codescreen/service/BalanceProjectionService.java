package dev.codescreen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BalanceProjectionService {
    private ConcurrentHashMap<String, BigDecimal> balances = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;



    public void updateBalance(String userId, BigDecimal amount, UpdateOperationType operationType, CardTypeDebitOrCredit type) {
        balances.compute(userId, (key, currentBalance) -> {
            if (currentBalance == null) {
                currentBalance = BigDecimal.ZERO;
            }
            return calculateNewBalance(currentBalance, amount, operationType, type);
        });
    }


    public void setBalance(String UserId, BigDecimal bigDecimal){
        balances.put(UserId, bigDecimal);
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, UpdateOperationType operationType, CardTypeDebitOrCredit type) {
        switch (operationType) {
            case ADD:
                return currentBalance.add(amount);
            case SUBTRACT:
                return subtractBalance(currentBalance, amount, type);
        }
        return currentBalance;
    }

    private BigDecimal subtractBalance(BigDecimal currentBalance, BigDecimal amount, CardTypeDebitOrCredit type) {
        BigDecimal newBalance = currentBalance.subtract(amount);
        if (type == CardTypeDebitOrCredit.DEBIT) {
            return newBalance.compareTo(BigDecimal.ZERO) < 0 ? currentBalance : newBalance;
        } else if (type == CardTypeDebitOrCredit.CREDIT) {
            return newBalance.compareTo(new BigDecimal("-2500")) < 0 ? currentBalance : newBalance;
        }
        return newBalance;
    }

    public BigDecimal getCurrentBalance(String userId) {
        return balances.getOrDefault(userId, BigDecimal.ZERO);
    }
}