package com.tts.component.redis;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tts.util.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/11 0011.
 */
public abstract class AbstractRedisTemplate {
    // 缓存统计信息打印,本身logback是异步打印,不需要自己起线程异步去打印了
    private final Logger logger = LoggerFactory.getLogger("cacheStatistic");

    private final Logger redisTimeOutLogger = LoggerFactory.getLogger("redisTimeOut");

    private final Logger warnLogger = LoggerFactory.getLogger(TTSRedisTemplate.class);

    private final static Integer TIMEOUT_THRESHOLD = 40;

    private static volatile boolean IS_CACHED = true;

    abstract TTSRedisTemplate<String, String> getRedisTemplate();

    abstract GracefulValueOperations<String, String> getValueOperations();

    abstract GracefulListOperations<String, String> getListOperations();

    abstract GracefulZSetOperations<String, String> getZsetOperations();


    public void setCached(boolean isCache) {
        IS_CACHED = isCache;
    }

    /**
     * @param key
     * @param value
     * @param timeout
     *            默认单位秒
     */
    public void setEx(String key, Object value, long timeout) {
        if (!IS_CACHED) {
            return;
        }
        String redisValue = null;

        if (value instanceof String) {
            redisValue = (String) value;
        } else {
            redisValue = JSON.toJSONString(value);
        }
        long beginTime = System.currentTimeMillis();
        try {
            getValueOperations().set(key, redisValue, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            warnLogger.warn("add cache failed!!! cachekey is:{}", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("add into cache timeOut cachekey is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     *
     * @param map
     * @param timeout
     */
    public void mset(Map<String, ? extends Object> map, long timeout) {
        if (!IS_CACHED) {
            return;
        }
        if (MapUtils.isEmpty(map)) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        getRedisTemplate().mset(map, timeout);
        long costTime = System.currentTimeMillis() - beginTime;

        if (costTime > TIMEOUT_THRESHOLD) {
            redisTimeOutLogger.warn("add into cache timeOut map is:{} cost time:{}", map.keySet(), costTime);
        }
    }

    /**
     * @param key
     */
    public boolean hasKey(String key) {
        if (!IS_CACHED) {
            return false;
        }
        long beginTime = System.currentTimeMillis();
        try {
            // 此处是读的操作
            // TODO
            boolean hasKey = getRedisTemplate().hasKey(key);
            if (hasKey) {

            } else {

            }
            return hasKey;
        } catch (Exception e) {
            warnLogger.warn("hasKey cache failed!!! key is: {}", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("hasKey cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return false;
    }

    /**
     * setex命令不支持long expire 需要将这两个命令拆开,拆开之后命令就不是原子性的了,可能会出现set 命令成功 expire命令失败
     * 这样就会出现key永久有效的情况
     *
     * @param key
     */
    public void expire(String key, long timeout) {
        if (!IS_CACHED) {
            return;
        }

        long beginTime = System.currentTimeMillis();
        try {
            getRedisTemplate().longExpire(key, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            warnLogger.error("expire cache failed!!! key is: {}", key, e);
            // 如果设置有效期失败,将之前塞进去的key删除
            this.deleteQuietly(key);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("expire cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
    }

    public Long getExpire(String key) {
        if (!IS_CACHED) { return null; }
        long beginTime = System.currentTimeMillis();
        try {
            return getRedisTemplate().getExpire(key);
        } catch (Exception e) {
            warnLogger.error("Get expire failed!!! key is: {}", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("Get expire cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return null;
    }

    private void deleteQuietly(String key) {
        try {
            getRedisTemplate().delete(key);
        } catch (Exception e) {
            warnLogger.error("delete cache failed!!! key is: {}", key, e);
        }
    }

    /**
     * @param keys
     * @param clazz
     * @return 传入的都返回，有数据返回数据，没有数据返回 NULL
     */
    public <T> Map<String, T> mget(List<String> keys, Class<T> clazz) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        long redistime=0L;
        try {
            List<String> multiGet = getValueOperations().multiGet(keys);
            redistime=System.currentTimeMillis()-beginTime;
            int size = multiGet.size();
            Map<String, T> result = new HashMap<String, T>(size);
            String curItem;
            for (int i = 0; i < size; i++) {
                curItem = multiGet.get(i);
                if (null == curItem) {
                    result.put(keys.get(i), null);

                } else {
                    result.put(keys.get(i), getValue(curItem, clazz));

                }
            }
            return result;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! keys is: {}", keys, e);
            HashMap<String, T> hashMap = new HashMap<String, T>(keys.size());
            for (String key : keys) {
                hashMap.put(key, null);
            }
            return hashMap;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! keys is:{} cost time:{} redis time :{}", keys, costTime,redistime);
            }
        }
    }

    public List<String> multiGet(List<String> keys) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        long redisTime=0L;
        try {
            List<String> multiGet = getValueOperations().multiGet(keys);
            redisTime=System.currentTimeMillis()-beginTime;

            return multiGet;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! keys is: {}", keys, e);

            return Lists.newArrayList();
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! keys is:{} cost time:{} redis time :{}", keys, costTime,redisTime);
            }
        }
    }

    /**
     * 获取存在的 map 列表(不含 NULL)
     */
    public <T> Map<String, T> multiGetMapNotNULL(List<String> keys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(keys))  return null;
        Map<String, T> result = mget(keys, clazz);
        return Maps.filterEntries(result, entity -> entity.getValue() != null);
    }

    /**
     *
     * @param keys
     *            批量获取int类型的value
     * @return
     */
    public Map<String, Integer> mgetIntValue(List<String> keys) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        long redistime=0L;
        try {
            List<String> multiGet = getValueOperations().multiGet(keys);
            redistime=System.currentTimeMillis()-beginTime;
            int size = multiGet.size();
            Map<String, Integer> result = new HashMap<String, Integer>(size);
            String curItem;
            for (int i = 0; i < size; i++) {
                curItem = multiGet.get(i);
                if (null == curItem) {
                    result.put(keys.get(i), null);

                } else {
                    result.put(keys.get(i), Integer.valueOf(curItem));

                }
            }
            return result;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! keys is: {}", keys, e);
            HashMap<String, Integer> hashMap = new HashMap<String, Integer>(keys.size());
            for (String key : keys) {
                hashMap.put(key, null);
            }
            return hashMap;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! keys is:{} cost time:{} redis time :{}", keys, costTime,redistime);
            }
        }
    }

    /**
     * @param keys
     * @param clazz
     * @return
     */
    public <T> Map<String, List<T>> mgetList(final List<String> keys, final Class<T> clazz) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        long redistime=0L;
        try {
            List<String> multiGet = getValueOperations().multiGet(keys);
            redistime=System.currentTimeMillis()-beginTime;
            int size = multiGet.size();
            Map<String, List<T>> result = new HashMap<String, List<T>>(size);
            String curItem;
            for (int i = 0; i < size; i++) {
                curItem = multiGet.get(i);
                if (null == curItem) {
                    result.put(keys.get(i), null);
                } else {
                    result.put(keys.get(i), JSON.parseArray(curItem, clazz));
                }
            }
            return result;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! keys is: {}", keys, e);
            HashMap<String, List<T>> hashMap = new HashMap<String, List<T>>(keys.size());
            for (String key : keys) {
                hashMap.put(key, null);
            }
            return hashMap;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;
            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! keys is:{} cost time:{} redis time :{}", keys, costTime,redistime);
            }
        }
    }

    /**
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        if (!IS_CACHED) {
            return;
        }
        String redisValue = null;

        if (value instanceof String) {
            redisValue = (String) value;
        } else {
            redisValue = JSON.toJSONString(value);
        }
        long beginTime = System.currentTimeMillis();
        try {
            getValueOperations().set(key, redisValue);
        } catch (Exception e) {
            warnLogger.warn("add cache failed!!! cachekey is:{}", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;
            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("add into cache timeOut cachekey is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * 有序set的单个元素插入
     *
     * @param key
     * @param value
     */
    public void setSortedSet(String key, Object value, double score) {
        if (!IS_CACHED) {
            return;
        }
        String redisValue = null;

        if (value instanceof String) {
            redisValue = (String) value;
        } else {
            redisValue = JsonUtil.toString(value);
        }
        long beginTime = System.currentTimeMillis();
        try {
            getZsetOperations().add(key, redisValue, score);
        } catch (Exception e) {
            warnLogger.warn("add sortedSet cache failed!!! cachekey is " + key + "value is " + value, e);
        } finally {
            long costTime = System.currentTimeMillis() - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("add into cache timeOut cachekey is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * @param key
     * @param value
     */
    public void setIfAbsent(String key, Object value) {
        if (!IS_CACHED) {
            return;
        }
        String redisValue = null;

        if (value instanceof String) {
            redisValue = (String) value;
        } else {
            redisValue = JSON.toJSONString(value);
        }
        long beginTime = System.currentTimeMillis();
        try {
            getValueOperations().setIfAbsent(key, redisValue);
        } catch (Exception e) {
            warnLogger.warn("add cache failed!!! cachekey is:{}", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("add into cache timeOut cachekey is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        List<T> t = null;
        try {
            String value = getValueOperations().get(key);
            if (StringUtils.isBlank(value)) {

                logger.info("cache miss key is:{}", key);
                return null;
            }
            t = JSON.parseArray(value, clazz);

            logger.info("cache hit key is:{}", key);
            return t;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! key is:{}", key, e);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * redis list 获取值列表， 全部记录
     *
     * @param key
     * @param startIndex
     * @param endIndex
     * @return
     */
    public List<String> getValueList(String key, int startIndex, int endIndex) {
        if (!IS_CACHED) {
            return Lists.newArrayList();
        }
        long beginTime = System.currentTimeMillis();
        try {
            List<String> valueList = getListOperations().range(key, startIndex, endIndex);

            if (CollectionUtils.isEmpty(valueList)) {

                logger.info("cache miss key is:{}", key);
                return Lists.newArrayList();
            }

            logger.info("cache hit key is:{}", key);
            return valueList;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! key is:{}", key, e);
            return Lists.newArrayList();
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * redis list 设置， 左塞
     *
     * @param key
     * @param value
     * @return
     */
    public void leftSetValueList(String key, String value) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getListOperations().leftPush(key, value);
        } catch (Exception e) {
            warnLogger.warn("set cache failed!!! key is: {},value is:{}", key, value, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * redis list 设置， 左塞 (批量接口)
     *
     * @param key
     * @param values
     * @return
     */
    public void batchLeftSetValueList(String key, List<String> values) {
        if (!IS_CACHED) {
            return;
        }
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getListOperations().leftPushAll(key, values);
        } catch (Exception e) {
            warnLogger.warn("batchLeftSetValueList cache failed!!! key is:{} ,values is:{}", key, values, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("batchLeftSetValueList cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     *
     * @param key
     * @param value
     *            object
     */

    public void leftSetObjectValueList(String key, Object value) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getListOperations().leftPush(key, JSON.toJSONString(value));
        } catch (Exception e) {
            warnLogger.warn("set cache failed!!! key is:{},value is:{} ", key, value, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }

        }

    }

    /**
     *
     * @param key
     * @param values
     */
    public void batchLeftSetObjectValueList(String key, List<? extends Object> values) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        List<String> jsonValues = Lists.newArrayList();
        for (Object object : values) {
            jsonValues.add(JSON.toJSONString(object));
        }
        try {
            getListOperations().leftPushAll(key, jsonValues);
        } catch (Exception e) {
            warnLogger.warn("set cache failed!!! key is:{} ", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }

        }
    }

    /**
     *
     * @param key
     * @param values
     */
    public void batchRightSetObjectValueList(String key, List<? extends Object> values) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        List<String> jsonValues = Lists.newArrayList();
        for (Object object : values) {
            jsonValues.add(JSON.toJSONString(object));
        }
        try {
            getListOperations().rightPushAll(key, jsonValues);
        } catch (Exception e) {
            warnLogger.warn("set cache failed!!! key is:{} ", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }

        }
    }

    /**
     * redis list 设置， 右塞 (批量接口)
     *
     * @param key
     * @param values
     * @return
     */
    public void batchRightSetValueList(String key, List<String> values) {
        if (!IS_CACHED) {
            return;
        }
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getListOperations().rightPushAll(key, values);
        } catch (Exception e) {
            warnLogger.warn("batchLeftSetValueList cache failed!!! key is:{} ", key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("batchLeftSetValueList cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * @param key
     * @param startIndex
     * @param length
     * @param clazz
     * @return
     */
    public <T> List<T> getValueObjectList(String key, int startIndex, int length, Class<T> clazz) {
        if (!IS_CACHED) {
            return Lists.newArrayList();
        }
        long beginTime = System.currentTimeMillis();
        try {

            List<String> valueList = getListOperations().range(key, startIndex, length);
            if (CollectionUtils.isEmpty(valueList)) {

                logger.info("cache miss key is:{}", key);
                return Lists.newArrayList();
            }

            logger.info("cache hit key is:{}", key);
            List<T> list = Lists.newArrayList();
            for (String value : valueList) {
                T t = JSON.parseObject(value, clazz);
                if (null != t) {
                    list.add(t);
                }
            }
            return list;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! key is:{}", key, e);
            return Lists.newArrayList();
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }

        }

    }

    /**
     * @param key
     * @return
     */
    public String getString(String key) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        String value = null;
        try {
            value = getValueOperations().get(key);
            if (StringUtils.isBlank(value)) {

                logger.info("cache miss key is:{}", key);
            } else {

                logger.info("cache hit key is:{}", key);
            }
            return value;

        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! key is:{}", key, e);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    public Long incr(String key, long delta) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        try {
            return getValueOperations().increment(key, delta);

        } catch (Exception e) {
            warnLogger.warn("incr failed!!! key is:{}", key, e);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("incr to cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * @param key
     * @param clazz
     * @return
     */
    public <T> T get(String key, Class<T> clazz) {
        if (!IS_CACHED) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        T t = null;
        try {
            String value = getValueOperations().get(key);

            if (StringUtils.isBlank(value)) {

                logger.info("cache miss key is:{}", key);
                return null;
            }
            t = getValue(value, clazz);

            logger.info("cache hit key is:{}", key);
            return t;
        } catch (Exception e) {
            warnLogger.warn("get from cache failed!!! key is:{}", key, e);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;
            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * @param keys
     */
    public void delete(Collection<String> keys) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getRedisTemplate().delete(keys);
        } catch (Exception e) {
            warnLogger.warn("delete from cache failed!!! key is:{}", keys, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("delete from cache timeout!!! key is:{} cost time:{}", keys, costTime);
            }
        }

    }

    public void evictFromList(String key, String value, long count) {
        if (!IS_CACHED) {
            return;
        }
        long beginTime = System.currentTimeMillis();
        try {
            getListOperations().remove(key, count, value);
        } catch (Exception e) {
            warnLogger.warn("remove cache failed!!! key is: " + key + "; value = " + value, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * 查询某键值对应list列表的长度
     *
     * @param key
     * @return
     */
    public long getValueListCount(String key) {
        if (!IS_CACHED) {
            return 0;
        }
        long beginTime = System.currentTimeMillis();
        try {
            Long size = getListOperations().size(key);
            if (null == size) {

                return 0;
            } else {

                return size;
            }
        } catch (Exception e) {
            warnLogger.warn("getValueListCount cache failed!!! key is:{} ", key, e);
            return 0;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get from cache timeout!!! key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * 批量插入sortSet元素，只支持同一键值
     *
     * @param valueAndScoreMap
     *            键值为存储的值，map值为当前值的score(用于排序) 默认是升序
     */
    public void msetSortSet(String key, Map<Object, Double> valueAndScoreMap) {
        if (!IS_CACHED || MapUtils.isEmpty(valueAndScoreMap)) {
            return;
        }

        Set<ZSetOperations.TypedTuple<String>> typedTuples = new HashSet<ZSetOperations.TypedTuple<String>>(valueAndScoreMap.size());
        Object curValue;
        for (Map.Entry<Object, Double> entry : valueAndScoreMap.entrySet()) {
            final String curValueStr;
            curValue = entry.getKey();
            if (curValue instanceof String) {
                curValueStr = (String) curValue;
            } else {
                curValueStr = JSON.toJSONString(curValue);
            }

            typedTuples.add(new ZSetOperations.TypedTuple<String>() {
                @Override
                public int compareTo(ZSetOperations.TypedTuple<String> o) {
                    return this.getScore() >= o.getScore() ? 1 : -1;
                }

                @Override
                public String getValue() {
                    return curValueStr;
                }

                @Override
                public Double getScore() {
                    return entry.getValue();
                }
            });
        }

        long beginTime = System.currentTimeMillis();
        try {
            getZsetOperations().add(key, typedTuples);
        } catch (Exception e) {
            warnLogger.warn("add zSetOperations cache failed!!! map key is" + key + "; valueAndScoreMap is " + valueAndScoreMap, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("add into cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
    }

    /**
     * 查询sortedSet的长度
     */
    public Long countSortSet(String key) {
        if (!IS_CACHED) {
            return null;
        }

        long beginTime = System.currentTimeMillis();
        try {
            return getZsetOperations().size(key);
        } catch (Exception e) {
            warnLogger.warn("count zSetOperations cache failed!!! key is" + key, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("count cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return null;
    }

    /**
     * 查询sortedSet的长度
     */
    public Long removeSortSet(String key, Object... values) {
        if (!IS_CACHED || null == values || 0 == values.length) {
            return null;
        }

        long beginTime = System.currentTimeMillis();
        try {
            return getZsetOperations().remove(key, values);
        } catch (Exception e) {
            warnLogger.warn("remove zSetOperations cache failed!!! key is" + key + "; values is " + values, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("count cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return null;
    }

    /**
     * 分页查询sortedSet 查询失败则返回空的set
     */
    public Set<String> rangeSortSet(String key, long start, long end) {
        if (!IS_CACHED) {
            return null;
        }

        long beginTime = System.currentTimeMillis();
        try {
            return getZsetOperations().reverseRange(key, start, end);
        } catch (Exception e) {
            warnLogger.warn("range zSetOperations cache failed!!! key is" + key + "; start is " + start + "; end is " + end, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("range cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return new HashSet<String>(0);
    }

    /**
     * 查询sortSet中有无当前的值 是否在set已有
     */
    public boolean existSortSet(String key, Object value) {
        if (!IS_CACHED) {
            return false;
        }

        long beginTime = System.currentTimeMillis();
        try {
            return null != getZsetOperations().score(key, value);
        } catch (Exception e) {
            warnLogger.warn("score zSetOperations cache failed!!! key is" + key + "; value is " + value, e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("score cache timeOut key is:{} cost time:{}", key, costTime);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(String value, Class<T> clazz) {
        T t = null;
        if (clazz.equals(String.class)) {
            t = (T) value;
        } else if (clazz.equals(Integer.class)) {
            t = (T) Integer.valueOf(value);
        } else {
            try
            {
                t = JSON.parseObject(value, clazz);
            }catch (Exception e) {
                warnLogger.warn("get value cache failed!!! value is: "+ value,e);
            }
        }
        return t;
    }

    /**
     * 获取keys
     * @param pattern
     * @return
     */
    public Set<String> getKeys(String pattern) {
        long beginTime = System.currentTimeMillis();
        try {
            return getRedisTemplate().keys(pattern);
        } catch (Exception e) {
            warnLogger.warn("getKeys failed!!! keys is: "+ pattern,e);
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - beginTime;

            if (costTime > TIMEOUT_THRESHOLD) {
                redisTimeOutLogger.warn("get keys cache timeOut pattern is:{} cost time:{}", pattern, costTime);
            }
        }
        return Sets.newHashSet();
    }

    /**
     * 批量获取keys
     * @param keysPattern
     * @return
     */
    public Set<String> getKeysBatch(Set<String> keysPattern) {
        Set<String> keysSet = Sets.newHashSet();
        List<Object> results = this.getRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                connection.openPipeline();
                RedisSerializer<String> keySerializer=new StringRedisSerializer();
                for (String keyPattern : keysPattern) {
                    connection.keys(keySerializer.serialize(keyPattern));
                }
                return null;
            }
        });
        for (Object result: results){
            if (result instanceof LinkedHashSet) {
                LinkedHashSet result1 = (LinkedHashSet) result;
                if (CollectionUtils.isNotEmpty(result1)) {
                    keysSet.addAll(result1);
                }
            }
        }
        return keysSet;
    }

}
