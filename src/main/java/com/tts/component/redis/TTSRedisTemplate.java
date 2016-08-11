package com.tts.component.redis;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.lucene.util.IOUtils.CHARSET_UTF_8;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 * RedisTemplate封装类
 * 根据缓存策略，集群方式不同，操作redis的方式也会不同
 */
public class TTSRedisTemplate<K,V> extends RedisTemplate<K,V> implements BeanNameAware {

    private RedisTemplate<K,V> redisTemplate;

    private String beanName;

    private RedisSerializer redisSerializer;

    private RedisSerializer defaultSerializer = new StringRedisSerializer();

    public RedisTemplate<K, V> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public void mset(Map<String, ? extends Object> map, long timeout) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        Map<String, String> cacheMap = new HashMap<String, String>(map.size());
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof String) {
                cacheMap.put(key, (String) value);
            } else if (value instanceof Integer) {
                cacheMap.put(key, value + "");
            } else {
                cacheMap.put(key, JSON.toJSONString(value));
            }
        }
        try {
            executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.openPipeline();
                    for (Map.Entry<String, String> item : cacheMap.entrySet()) {
                        connection.setEx(getRedisSerializer().serialize(item.getKey()), timeout, null == item.getValue() ? null : getRedisSerializer().serialize(item.getValue()));
                    }
                    // 无需返回是否缓存成功
                    return null;
                }
            });
        } catch (Exception e) {
            logger.warn("mset into redis failed :{}",e);
        }
    }

    public Boolean longExpire(K key, final long timeout, final TimeUnit unit) {

        try {
            final byte[] rawKey = String.valueOf(key).getBytes("UTF-8");

            return (Boolean) this.execute(new RedisCallback() {
                @Override
                public Boolean doInRedis(RedisConnection connection) {
                    return connection.expire(rawKey, TimeoutUtils.toSeconds(timeout, unit));
                }
            }, true);
        } catch (UnsupportedEncodingException e) {
            logger.warn("unsupport this encoding :{}",e);
        }

        return false;
    }

    public RedisSerializer getRedisSerializer() {
        // 没有自定义则返回默认序列化方式
        if (null == redisSerializer) {
            return defaultSerializer;
        }
        return redisSerializer;
    }

    public void setRedisSerializer(RedisSerializer redisSerializer) {
        this.redisSerializer = redisSerializer;
    }


    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }
}
