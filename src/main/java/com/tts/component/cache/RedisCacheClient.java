package com.tts.component.cache;

import com.google.common.collect.Lists;
import com.tts.component.redis.GracefulRedisTemplate;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/15 0015.
 */
public class RedisCacheClient implements CacheClient{

    private GracefulRedisTemplate gracefulRedisTemplate;

    @Override
    public void set(String key, Object value) {
        gracefulRedisTemplate.set(key,value);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        gracefulRedisTemplate.setEx(key,value,timeout);
    }

    @Override
    public <T> T get(String key, Class<T> classType) {
        return gracefulRedisTemplate.get(key,classType);
    }

    @Override
    public void delete(String key) {
        gracefulRedisTemplate.delete(Lists.newArrayList(key));
    }

    public void setGracefulRedisTemplate(GracefulRedisTemplate gracefulRedisTemplate) {
        this.gracefulRedisTemplate = gracefulRedisTemplate;
    }
}
