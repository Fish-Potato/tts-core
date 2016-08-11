package com.tts.component.redis;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 * ValueOperations封装类
 * 根据缓存策略，集群方式不同，操作redis的方式也会不同
 */
public class GracefulValueOperations<K,V> implements BeanNameAware {

    private ValueOperations<K,V> valueOperations;

    private String beanName;


    public ValueOperations<K, V> getValueOperations() {
        return valueOperations;
    }

    public void setValueOperations(ValueOperations<K, V> valueOperations) {
        this.valueOperations = valueOperations;
    }

    public void set(K key,V value) {
        valueOperations.set(key,value);
    }

    public void set(K key, V value, long timeout, TimeUnit unit) {
        valueOperations.set(key,value,timeout, unit);
    }

    public List<V> multiGet(Collection<K> keys) {
        return valueOperations.multiGet(keys);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }

    public void setIfAbsent(K key, V value) {
        valueOperations.setIfAbsent(key,value);
    }

    public V get(K key) {
        return valueOperations.get(key);
    }

    public Long increment(K key, long delta) {
        return valueOperations.increment(key,delta);
    }
}
