package com.tts.component.redis;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 */
public class GracefulZSetOperations<K,V> implements BeanNameAware {

    private ZSetOperations<K,V> zSetOperations;

    private String beanName;

    public ZSetOperations<K, V> getzSetOperations() {
        return zSetOperations;
    }

    public void setzSetOperations(ZSetOperations<K, V> zSetOperations) {
        this.zSetOperations = zSetOperations;
    }

    public Boolean add(K key, V value, double score) {
        return zSetOperations.add(key, value, score);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }

    public void add(K key, Set<ZSetOperations.TypedTuple<V>> typedTuples) {
        zSetOperations.add(key,typedTuples);
    }

    public Double score(K key, Object value) {
        return zSetOperations.score(key,value);
    }

    public Set<V> reverseRange(K key, long start, long end) {
        return zSetOperations.reverseRange(key,start,end);
    }

    public Long size(K key) {
        return zSetOperations.size(key);
    }

    public Long remove(K key, Object[] values) {
        return zSetOperations.remove(key,values);
    }
}
