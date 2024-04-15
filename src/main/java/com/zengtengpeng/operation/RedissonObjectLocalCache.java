package com.zengtengpeng.operation;

import com.github.benmanes.caffeine.cache.Cache;
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

/**
 * 操作对象,带本地缓存(请注意,使用了改类,一定要调用这个类的delete来清除缓存.不然本地缓存将无法刷新)
 * 次对象,默认的key就是redis的key.无法使用多个 Cache 记录,如果要使用多个Cache 请使用 RedissonObjectMultiLocalCache
 */
public class RedissonObjectLocalCache {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedissonProperties redissonProperties;


    @Autowired
    private Cache<String,Object> cache;


    @Autowired
    private RTopic topic;
    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name) {
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
    public <T> T getValue(String name, RealData<T> realData) {
        return getValue(name, realData, redissonProperties.getDataValidTime());
    }

    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name, RealData<T> realData, Long time) {
        T value = getValue(name);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(name);
            } else {
                //否则insert
                setValue(name, value, time);
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
    public <T> T getValue(String name, RealData<T> realData, DataCache<T> dataCache, Long time) {
        T value = getValue(name);
        if (value == null) {
            value = realData.get();
            if (ObjectUtils.isEmpty(value)) {
                //如果是空的,则删除
                delete(name);
            } else {
                Boolean cache = dataCache.isCache(value);
                if (cache) {
                    //否则insert
                    setValue(name, value, time);
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
    public <T> void setValue(String name, T value) {
        setValue(name, value, redissonProperties.getDataValidTime());
    }

    /**
     * 设置对象的值
     *
     * @param name  键
     * @param value 值
     * @param time  缓存时间 单位毫秒 -1 永久缓存
     * @return
     */
    public <T> void setValue(String name, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(name);
        if (time == -1) {
            bucket.set(value);
        } else {
            bucket.set(value, Duration.ofMillis(time));
        }

        topic.publish(new LocalCacheKeyVo(name));
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param name  键
     * @param value 值
     * @param time  缓存时间 单位毫秒
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String name, T value, Long time) {
        RBucket<Object> bucket = redissonClient.getBucket(name);
        boolean b;
        if (time == -1) {
            b = bucket.setIfAbsent(value);
        } else {
            b = bucket.setIfAbsent(value, Duration.ofMillis(time));
        }
        topic.publish(new LocalCacheKeyVo(name));
        return b;
    }

    /**
     * 如果值已经存在则则不设置
     *
     * @param name  键
     * @param value 值
     * @return true 设置成功,false 值存在,不设置
     */
    public <T> Boolean trySetValue(String name, T value) {
        return trySetValue(name, value, redissonProperties.getDataValidTime());
    }

    /**
     * 删除对象
     *
     * @param name 键
     * @return true 删除成功,false 不成功
     */
    public Boolean delete(String name) {

        boolean b = redissonClient.getKeys().delete(name) > 0;

        topic.publish(new LocalCacheKeyVo(name));
        return b;
    }

    /**
     * 基于key清理本地缓存
     */
    public Boolean clearLocalCache(String name) {
        return topic.publish(new LocalCacheKeyVo(name))>0;
    }


}
