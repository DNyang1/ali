package com.finalProject.ali.checkout.service;

import com.finalProject.ali.checkout.dto.CheckoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CheckoutRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration CHECKOUT_TTL = Duration.ofMinutes(10);

    private String key(String userId) {
        return "checkout:" + userId;
    }

    public void save(String userId, CheckoutResponse response){
        redisTemplate.opsForValue()
                .set(
                    key(userId),
                    response,
                    CHECKOUT_TTL
        );

        System.out.println(
                "[REDIS][SAVE] key=" + key(userId) +
                        ", ttl=" + CHECKOUT_TTL.toMinutes() + "min"
        );

    }

    public CheckoutResponse get(String userId) {

        System.out.println(
                "[REDIS][GET] key=" + key(userId) +
                        ", hit=" + (redisTemplate.opsForValue()
                        .get(
                                key(userId)
                        ) != null)
        );

        return (CheckoutResponse) redisTemplate.opsForValue()
                .get(
                        key(userId)
                );
    }

    public void delete(String userId) {
        Boolean deleted = redisTemplate.delete(key(userId));
        System.out.println(
                "[REDIS][DELETE] key=" + key(userId) +
                        ", success=" + deleted
        );
    }
}