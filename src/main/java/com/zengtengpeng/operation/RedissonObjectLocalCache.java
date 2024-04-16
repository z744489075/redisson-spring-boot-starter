package com.zengtengpeng.operation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zengtengpeng.bean.CaffeineTimeBeanVo;
import com.zengtengpeng.bean.LocalCacheKeyVo;
import com.zengtengpeng.func.DataCache;
import com.zengtengpeng.func.RealData;
import com.zengtengpeng.properties.RedissonProperties;
import com.zengtengpeng.utils.LocalDataUtils;
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
 * 次对象,默认的key就是redis的key.无法使用多个 Cache 记录,如果要使用多个Cache 请使用 RedissonObjectLocalCache
 */
public class RedissonObjectLocalCache {

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
     * @param redisKey redis的key
     * @param localCacheKey 本地缓存的key
     * @param <T>
     * @return
     */
    public <T> T getValue(String redisKey,String localCacheKey) {
        return getValue(redisKey,localCacheKey,new CaffeineTimeBeanVo());
    }


    /**
     * 获取对象值
     *
     * @param redisKey redis的key
     * @param localCacheKey 本地缓存的key
     * @param <T>
     * @return
     */
    public <T> T getValue(String redisKey,String localCacheKey, CaffeineTimeBeanVo caffeineTimeBeanVo) {
        Cache<String, Object> cache = cacheMap.get(localCacheKey);
        if(cache == null) {
            if(caffeineTimeBeanVo==null){
                caffeineTimeBeanVo=new CaffeineTimeBeanVo();
            }
            cache= Caffeine.newBuilder()
                    //设置过期时间
                    .expireAfterWrite(caffeineTimeBeanVo.getLocalCacheMultiTime(), TimeUnit.MILLISECONDS)
                    //初始容量为
                    .initialCapacity(caffeineTimeBeanVo.getLocalInitMultiSize())
                    //最大容量为
                    .maximumSize(caffeineTimeBeanVo.getLocalMaxMultiSize())
                    .build();
            cacheMap.put(localCacheKey,cache);
        }
        Object ifPresent = cache.getIfPresent(redisKey);
        if(ObjectUtils.isEmpty(ifPresent)) {
            RBucket<T> bucket = redissonClient.getBucket(redisKey);
            ifPresent = bucket.get();
            if(ifPresent!=null) {
                cache.put(redisKey, ifPresent);
            }
        }
        return (T) ifPresent;
    }

    /**
     * 获取对象值
     *
     * @param redisKey redis的key
     * @param <T>
     * @return
     */
    public <T> T getValueNoLocalCache(String redisKey) {
        RBucket<T> bucket = redissonClient.getBucket(redisKey);
        return bucket.get();
    }


    /**
     * 获取对象值
     *
     * @param redisKey
     * @param <T>
     * @return
     */
    public <T> T getValue(String redisKey,String localCacheKey, RealData<T> realData) {
        return getValue(redisKey,localCacheKey, realData, redissonProperties.getDataValidTime(),null);
    }


    /**
     * 获取对象值
     *
     * @param redisKey
     * @param <T>
     * @return
     */
    public <T> T getValueNoLocalCache(String redisKey,String localCacheKey, RealData<T> realData) {
        return getValueNoLocalCache(redisKey,localCacheKey, realData, redissonProperties.getDataValidTime());
    }

    /**
     * 获取对象值
     *
     * @param redisKey
     * @param <T>
     * @return
     */
    public <T> T getValue(String redisKey,String localCacheKey, RealData<T> realData, Long time, CaffeineTimeBeanVo caffeineTimeBeanVo) {
        T value = getValue(redisKey,localCacheKey,caffeineTimeBeanVo);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(redisKey,localCacheKey);
            } else {
                //否则insert
                setValue(redisKey,localCacheKey, value, time);
            }
        }
        return value;
    }

    /**
     * 获取对象值
     *
     * @param redisKey
     * @param <T>
     * @return
     */
    public <T> T getValueNoLocalCache(String redisKey,String localCacheKey, RealData<T> realData, Long time) {
        T value = getValueNoLocalCache(redisKey);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(redisKey,localCacheKey);
            } else {
                //否则insert
                setValue(redisKey,localCacheKey, value, time);
            }
        }
        return value;
    }

    /**
     * 获取对象值
     *
     * @param redisKey
     * @param <T>
     * @return
     */
    public <T> T getValue(String redisKey,String localCacheKey, RealData<T> realData, DataCache<T> dataCache,
                          Long time, CaffeineTimeBeanVo caffeineTimeBeanVo) {
        T value = getValue(redisKey,localCacheKey,caffeineTimeBeanVo);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(redisKey,localCacheKey);
            } else {
                Boolean cache = dataCache.isCache(value);
                if (cache) {
                    //否则insert
                    setValue(redisKey,localCacheKey, value, time);
                }
            }
        }
        return value;
    }

    /**
     * 设置对象的值
     *
     * @param redisKey  键
     * @param value 值
     * @return
     */
    public <T> void setValue(String redisKey,String localCacheKey, T value) {
        setValue(redisKey,localCacheKey, value, redissonProperties.getDataValidTime());
    }

    /**
     * 设置对象的值
     *
     * @param redisKey  键
     * @param value 值
     * @param time  缓存时间 单位毫秒 -1 永久缓存
     * @return
     */
    public <T> void setValue(String redisKey,String localCacheKey, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(redisKey);
        if (time == -1) {
            bucket.set(value);
        } else {
            bucket.set(value, Duration.ofMillis(time));
        }
        LocalCacheKeyVo message = new LocalCacheKeyVo(1, localCacheKey, redisKey);

        LocalDataUtils.clearLocalData(message);
        topic.publish(message);
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param redisKey  键
     * @param value 值
     * @param time  缓存时间 单位毫秒
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String redisKey,String localCacheKey, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(redisKey);
        boolean b;
        if (time == -1) {
            b = bucket.setIfAbsent(value);
        } else {
            b = bucket.setIfAbsent(value, Duration.ofMillis(time));
        }
        LocalCacheKeyVo message = new LocalCacheKeyVo(1, localCacheKey, redisKey);
        LocalDataUtils.clearLocalData(message);
        topic.publish(message);
        return b;
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param redisKey  键
     * @param value 值
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String redisKey,String localCacheKey, T value) {
        return trySetValue(redisKey,localCacheKey, value, redissonProperties.getDataValidTime());
    }

    /**
     * 清理缓存
     * @author ztp
     * @date 2024/4/15 18:35
     * @param redisKey 需要清理的redisKey,该值如何为空则清空 localCacheKey下的所有缓存
     * @param localCacheKey 本地缓存key
     * @return java.lang.Boolean
    */
    public Boolean delete(String redisKey,String localCacheKey) {

        boolean b = redissonClient.getKeys().delete(redisKey) > 0;

        LocalCacheKeyVo message = new LocalCacheKeyVo(1, localCacheKey, redisKey);
        LocalDataUtils.clearLocalData(message);
        topic.publish(message);
        return b;
    }

    /**
     * 基于key清理本地缓存
     * @author ztp
     * @date 2024/4/15 18:33
     * @param redisKey 需要清理的redisKey,该值如何为空则清空 localCacheKey下的所有缓存
     * @param localCacheKey 本地缓存key
     * @return java.lang.Boolean
    */
    public Boolean clearLocalCache(String redisKey,String localCacheKey) {
        LocalCacheKeyVo message = new LocalCacheKeyVo(1, localCacheKey, redisKey);
        LocalDataUtils.clearLocalData(message);
        return topic.publish(message)>0;
    }


}
