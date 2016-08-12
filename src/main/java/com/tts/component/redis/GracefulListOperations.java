package com.tts.component.redis;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.ListOperations;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 * ListOperations封装类
 * 根据缓存策略，集群方式不同，操作redis的方式也会不同
 */
public class GracefulListOperations<K,V> implements BeanNameAware {

    private ListOperations<K,V> listOperations;

    private String beanName;

    public ListOperations<K, V> getListOperations() {
        return listOperations;
    }

    public void setListOperations(ListOperations<K, V> listOperations) {
        this.listOperations = listOperations;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }

    public List<V> range(K key, int startIndex, int endIndex) {
        return listOperations.range(key,startIndex,endIndex);
    }

    public void leftPush(K key, V value) {
        listOperations.leftPush(key,value);
    }

    public void leftPushAll(K key, Collection<V> values) {
        listOperations.leftPushAll(key,values);
    }

    public void rightPushAll(K key, Collection<V> values) {
        listOperations.rightPushAll(key, values);
    }

    public void remove(K key, long count, V value) {
        listOperations.remove(key,count,value);
    }

    public Long size(K key) {
        return listOperations.size(key);
    }
}
