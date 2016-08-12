package com.tts.component.redis;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 */
public class GracefulSetOperations<K,V> implements BeanNameAware {

    private SetOperations<K,V> setOperations;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }

    public String getBeanName() {
        return beanName;
    }

    public SetOperations<K, V> getSetOperations() {
        return setOperations;
    }

    public void setSetOperations(SetOperations<K, V> setOperations) {
        this.setOperations = setOperations;
    }

    public Set<V> difference(K key, K otherKey) {
        return setOperations.difference(key, otherKey);
    }

    public Set<V> difference(K key, Collection<K> otherKeys) {
        return setOperations.difference(key, otherKeys);
    }

    public Long differenceAndStore(K key, K otherKey, K destKey) {
        return setOperations.differenceAndStore(key, otherKey, destKey);
    }

    public Long differenceAndStore(K key, Collection<K> otherKeys, K destKey) {
        return setOperations.differenceAndStore(key, otherKeys, destKey);
    }

    public Set<V> intersect(K key, K otherKey) {
        return setOperations.intersect(key, otherKey);
    }

    public Set<V> intersect(K key, Collection<K> otherKeys) {
        return setOperations.intersect(key, otherKeys);
    }

    public Long intersectAndStore(K key, K otherKey, K destKey) {
        return setOperations.intersectAndStore(key, otherKey, destKey);
    }

    public Long intersectAndStore(K key, Collection<K> otherKeys, K destKey) {
        return setOperations.intersectAndStore(key, otherKeys, destKey);
    }

    public Set<V> union(K key, K otherKey) {
        return setOperations.union(key, otherKey);
    }

    public Set<V> union(K key, Collection<K> otherKeys) {
        return setOperations.union(key, otherKeys);
    }

    public Long unionAndStore(K key, K otherKey, K destKey) {
        return setOperations.unionAndStore(key, otherKey, destKey);
    }

    public Long unionAndStore(K key, Collection<K> otherKeys, K destKey) {
        return setOperations.unionAndStore(key, otherKeys, destKey);
    }

    public Long add(K key, V... values) {
        return setOperations.add(key, values);
    }

    public Boolean isMember(K key, Object o) {
        return setOperations.isMember(key, o);
    }

    public Set<V> members(K key) {
        return setOperations.members(key);
    }

    public Boolean move(K key, V value, K destKey) {
        return setOperations.move(key, value, destKey);
    }

    public V randomMember(K key) {
        return setOperations.randomMember(key);
    }

    public Long remove(K key, Object... values) {
        Long result = setOperations.remove(key, values);
        return result;
    }

    public V pop(K key) {
        return setOperations.pop(key);
    }

    public Long size(K key) {
        return setOperations.size(key);
    }
}
