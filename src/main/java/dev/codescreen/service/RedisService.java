package dev.codescreen.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public void updateBalance(String userId, BigDecimal newBalance) {
        stringRedisTemplate.opsForValue().set("userBalance:" + userId, newBalance.toPlainString());
    }

    public void printAllEvents() {
        Set<String> keys = stringRedisTemplate.keys("dev.codescreen.model.*");
        keys.forEach(key -> {
            String data = stringRedisTemplate.opsForValue().get(key);
            System.out.println("Key: " + key + ", Data: " + data);
        });
    }
}
