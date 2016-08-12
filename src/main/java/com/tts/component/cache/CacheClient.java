package com.tts.component.cache;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 */
public interface CacheClient<K,V> {

    void set(K key,V value);

    void set(K key, V value, long timeout, TimeUnit unit);

    V get(K key, Class<?> classType);

    void delete(K key);
}
