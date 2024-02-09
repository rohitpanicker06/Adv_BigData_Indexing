package com.example.demo1.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Repository
public class PlanDAO {

    @Autowired
    private Jedis jedis;

    public void hSet(String key, String field, String value) {
        jedis.hset(key, field, value);
    }

    public boolean checkIfExists(String key) {
        return jedis.exists(key);
    }


    public long del(String key) {
        return jedis.del(key);
    }

    public String get(String key) {
        return jedis.get(key);
    }


    public String hGet(String key, String field) {
        return jedis.hget(key, field);
    }

    public Map<String, String> getAllValuesByKey(String key) {
        return jedis.hgetAll(key);
    }
}
