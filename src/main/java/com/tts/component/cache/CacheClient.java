package com.tts.component.cache;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 */
public interface CacheClient {

    void set(String key,Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    <T> T get(String key, Class<T> classType);

    void delete(String key);
}
