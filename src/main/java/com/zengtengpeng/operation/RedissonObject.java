package com.zengtengpeng.operation;

import com.zengtengpeng.func.DataCache;
import com.zengtengpeng.func.RealData;
import com.zengtengpeng.properties.RedissonProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 操作对象
 */
public class RedissonObject {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonProperties redissonProperties;

    /**
     * 获取对象值
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getValue(String name) {
        RBucket<T> bucket = redissonClient.getBucket(name);
        return bucket.get();
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
     * 获取对象空间
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> RBucket<T> getBucket(String name) {
        return redissonClient.getBucket(name);
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

        return redissonClient.getKeys().delete(name)>0;
    }


}
