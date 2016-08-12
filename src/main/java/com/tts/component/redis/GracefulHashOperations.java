package com.tts.component.redis;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.HashOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 *
 */
public class GracefulHashOperations<H,HK,HV> implements BeanNameAware {

    private HashOperations<H,HK,HV> hashOperations;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public HashOperations<H, HK, HV> getHashOperations() {
        return hashOperations;
    }

    public void setHashOperations(HashOperations<H, HK, HV> hashOperations) {
        this.hashOperations = hashOperations;
    }

    public void delete(H key, Object... hashKeys) {
        hashOperations.delete(key, hashKeys);
    }

    public Boolean hasKey(H key, Object hashKey) {
        return hashOperations.hasKey(key, hashKey);
    }

    public HV get(H key, Object hashKey) {
        return hashOperations.get(key, hashKey);
    }

    public List<HV> multiGet(H key, Collection<HK> hashKeys) {
        return hashOperations.multiGet(key, hashKeys);
    }

    public Long increment(H key, HK hashKey, long delta) {
        return hashOperations.increment(key, hashKey, delta);
    }

    public Double increment(H key, HK hashKey, double delta) {
        return hashOperations.increment(key, hashKey, delta);
    }

    public Set<HK> keys(H key) {
        return hashOperations.keys(key);
    }

    public Long size(H key) {
        return hashOperations.size(key);
    }

    public void putAll(H key, Map<? extends HK, ? extends HV> m) {
        hashOperations.putAll(key, m);
    }

    public void put(H key, HK hashKey, HV value) {
        hashOperations.put(key, hashKey, value);
    }

    public Boolean putIfAbsent(H key, HK hashKey, HV value) {
        return hashOperations.putIfAbsent(key, hashKey, value);
    }

    public List<HV> values(H key) {
        return hashOperations.values(key);
    }

    public Map<HK, HV> entries(H key) {
        return hashOperations.entries(key);
    }
}
