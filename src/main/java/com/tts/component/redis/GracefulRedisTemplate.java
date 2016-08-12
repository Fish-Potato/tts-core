package com.tts.component.redis;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 * redis template封装类，封装了:
 * @see TTSRedisTemplate
 * @see GracefulValueOperations
 * @see GracefulListOperations
 * @see GracefulZSetOperations
 *
 */
public class GracefulRedisTemplate extends AbstractRedisTemplate {

    private TTSRedisTemplate<String,String> redisTemplate;

    private GracefulValueOperations<String,String> valueOperations;

    private GracefulListOperations<String,String> listOperations;

    private GracefulZSetOperations<String,String> zSetOperations;

    @Override
    TTSRedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    GracefulValueOperations<String, String> getValueOperations() {
        return valueOperations;
    }

    @Override
    GracefulListOperations<String, String> getListOperations() {
        return listOperations;
    }

    @Override
    GracefulZSetOperations<String, String> getZsetOperations() {
        return zSetOperations;
    }

    public void setRedisTemplate(TTSRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValueOperations(GracefulValueOperations valueOperations) {
        this.valueOperations = valueOperations;
    }

    public void setListOperations(GracefulListOperations listOperations) {
        this.listOperations = listOperations;
    }

    public void setzSetOperations(GracefulZSetOperations zSetOperations) {
        this.zSetOperations = zSetOperations;
    }
}
