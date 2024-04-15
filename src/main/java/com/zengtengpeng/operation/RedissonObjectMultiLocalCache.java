package com.zengtengpeng.operation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zengtengpeng.configuration.LocalCacheKeyVo;
import com.zengtengpeng.func.DataCache;
import com.zengtengpeng.func.RealData;
import com.zengtengpeng.properties.RedissonProperties;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 操作对象,带本地缓存(请注意,使用了改类,一定要调用这个类的delete来清除缓存.不然本地缓存将无法刷新)
 * 次对象,默认的key就是redis的key.无法使用多个 Cache 记录,如果要使用多个Cache 请使用 RedissonObjectMultiLocalCache
 */
public class RedissonObjectMultiLocalCache {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedissonProperties redissonProperties;

    public static Map<String,Cache<String,Object>> cacheMap=new HashMap<>();


    @Autowired
    private RTopic topic;
    /**
     * 获取对象值
     *
     * @param name redis的key
     * @param localCacheKey 本地缓存的key
     * @param <T>
     * @return
     */
    public <T> T getValue(String name,String localCacheKey) {
        Cache<String, Object> cache = cacheMap.get(localCacheKey);
        if(cache == null) {
            cache= Caffeine.newBuilder()
                    //设置过期时间
                    .expireAfterWrite(redissonProperties.getLocalCacheMultiTime(), TimeUnit.MILLISECONDS)
                    //初始容量为
                    .initialCapacity(redissonProperties.getLocalInitMultiSize())
                    //最大容量为
                    .maximumSize(redissonProperties.getLocalMaxMultiSize())
                    .build();
            cacheMap.put(localCacheKey,cache);
        }
        Object ifPresent = cache.getIfPresent(name);
        if(ObjectUtils.isEmpty(ifPresent)) {
            RBucket<T> bucket = redissonClient.getBucket(name);
            ifPresent = bucket.get();
            if(ifPresent!=null) {
                cache.put(name, ifPresent);
            }
        }
        return (T) ifPresent;
    }

    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name,String localCacheKey, RealData<T> realData) {
        return getValue(name,localCacheKey, realData, redissonProperties.getDataValidTime());
    }

    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name,String localCacheKey, RealData<T> realData, Long time) {
        T value = getValue(name,localCacheKey);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(name,localCacheKey);
            } else {
                //否则insert
                setValue(name,localCacheKey, value, time);
            }
        }
        return value;
    }

    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name,String localCacheKey, RealData<T> realData, DataCache<T> dataCache, Long time) {
        T value = getValue(name,localCacheKey);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(name,localCacheKey);
            } else {
                Boolean cache = dataCache.isCache(value);
                if (cache) {
                    //否则insert
                    setValue(name,localCacheKey, value, time);
                }
            }
        }
        return value;
    }

    /**
     * 设置对象的值
     *
     * @param name  键
     * @param value 值
     * @return
     */
    public <T> void setValue(String name,String localCacheKey, T value) {
        setValue(name,localCacheKey, value, redissonProperties.getDataValidTime());
    }

    /**
     * 设置对象的值
     *
     * @param name  键
     * @param value 值
     * @param time  缓存时间 单位毫秒 -1 永久缓存
     * @return
     */
    public <T> void setValue(String name,String localCacheKey, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(name);
        if (time == -1) {
            bucket.set(value);
        } else {
            bucket.set(value, Duration.ofMillis(time));
        }
        topic.publish(new LocalCacheKeyVo(name,localCacheKey));
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param name  键
     * @param value 值
     * @param time  缓存时间 单位毫秒
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String name,String localCacheKey, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(name);
        boolean b;
        if (time == -1) {
            b = bucket.setIfAbsent(value);
        } else {
            b = bucket.setIfAbsent(value, Duration.ofMillis(time));
        }
        topic.publish(new LocalCacheKeyVo(name,localCacheKey));
        return b;
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param name  键
     * @param value 值
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String name,String localCacheKey, T value) {
        return trySetValue(name,localCacheKey, value, redissonProperties.getDataValidTime());
    }

    /**
     * 删除对象
     *
     * @param name 键
     * @return true 删除成功,false 不成功
     */
    public Boolean delete(String name,String localCacheKey) {

        boolean b = redissonClient.getKeys().delete(name) > 0;

        topic.publish(new LocalCacheKeyVo(name,localCacheKey));
        return b;
    }

    /**
     * 基于key清理本地缓存
     */
    public Boolean clearLocalCache(String name,String localCacheKey) {
        return topic.publish(new LocalCacheKeyVo(name,localCacheKey))>0;
    }


}
