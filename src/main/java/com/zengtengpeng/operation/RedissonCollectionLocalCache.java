package com.zengtengpeng.operation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zengtengpeng.bean.CaffeineTimeBeanVo;
import com.zengtengpeng.bean.LocalCacheKeyVo;
import com.zengtengpeng.func.*;
import com.zengtengpeng.properties.RedissonProperties;
import com.zengtengpeng.utils.LocalDataUtils;
import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 操作集合
 */
public class RedissonCollectionLocalCache {

    private static final Logger log = LoggerFactory.getLogger(RedissonCollectionLocalCache.class);
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonProperties redissonProperties;


    public static Map<String, Cache<String, Object>> cacheMap = new HashMap<>();


    @Autowired
    private RTopic topic;

    /**
     * 获取map集合对应的key值
     *
     * @param redisKey
     * @return
     */
    public <T> T getMapValue(String redisKey, String mapKey) {
        return getMapValue(redisKey,mapKey,new CaffeineTimeBeanVo());
    }
    /**
     * 获取map集合对应的key值
     *
     * @param redisKey
     * @return
     */
    public <T> T getMapValue(String redisKey, String mapKey, CaffeineTimeBeanVo caffeineTimeBeanVo) {
        /*RMap<Object, T> map = getMap(redisKey);
        return map.get(mapKey);*/
        Cache<String, Object> cache = cacheMap.get(redisKey);
        if (cache == null) {
            if (caffeineTimeBeanVo == null) {
                caffeineTimeBeanVo = new CaffeineTimeBeanVo();
            }
            cache = Caffeine.newBuilder()
                    //设置过期时间
                    .expireAfterWrite(caffeineTimeBeanVo.getLocalCacheMultiTime(), TimeUnit.MILLISECONDS)
                    //初始容量为
                    .initialCapacity(caffeineTimeBeanVo.getLocalInitMultiSize())
                    //最大容量为
                    .maximumSize(caffeineTimeBeanVo.getLocalMaxMultiSize())
                    .build();
            cacheMap.put(redisKey, cache);
        }
        Object ifPresent = cache.getIfPresent(mapKey);
        if (ObjectUtils.isEmpty(ifPresent)) {
            RMap<String, T> map = redissonClient.getMap(redisKey);
            ifPresent = map.get(mapKey);
            if (ifPresent != null) {
                cache.put(mapKey, ifPresent);
            }
        }
        return (T) ifPresent;
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param redisKey
     * @return
     */
    public <T> T getMapValue(String redisKey, String mapKey, RealData<T> realData) {

        return getMapValue(redisKey, mapKey, realData, redissonProperties.getDataValidTime(), null);
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param redisKey
     * @return
     */
    public <T> T getMapValue(String redisKey, String mapKey, RealData<T> realData,
                             Long time, CaffeineTimeBeanVo caffeineTimeBeanVo) {
        Object o = getMapValue(redisKey, mapKey, caffeineTimeBeanVo);
        if (o == null) {
            o = realData.get();
            if (ObjectUtils.isEmpty(o)) {
                redissonClient.getMap(redisKey).remove(mapKey);
            } else {
                setMapValue(redisKey, mapKey, o, time);
            }
        }
        return (T) o;
    }


    /**
     * 设置 map值
     *
     * @param redisKey
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public <T> void setMapValue(String redisKey, String mapKey, T value, Long time) {
        RMap<String, T> map = redissonClient.getMap(redisKey);
        map.put(mapKey, value);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            map.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            map.expire(Duration.ofMillis(time));
        }
        LocalCacheKeyVo message = new LocalCacheKeyVo(2, redisKey, mapKey);

        LocalDataUtils.clearLocalData(message);
        topic.publish(message);
    }


    /**
     * 设置map集合
     *
     * @param redisKey
     * @return
     */
    public void setMapValue(String redisKey, String mapKey, Object value) {
        setMapValue(redisKey, mapKey, value, redissonProperties.getDataValidTime());
    }


    /**
     * 删除对象
     * @author ztp
     * @date 2024/4/15 19:42
     * @param redisKey 需要删除的key
     * @param mapKey 如果为null,则删除整个map
     * @return void
    */
    public void delete(String redisKey, String mapKey) {

        if(mapKey==null) {
           redissonClient.getKeys().delete(redisKey);
        }else {
            redissonClient.getMap(redisKey).remove(mapKey);
        }
        LocalCacheKeyVo message = new LocalCacheKeyVo(2, redisKey, mapKey);
        LocalDataUtils.clearLocalData(message);
        topic.publish(message);
    }


    /**
     * 清理本地缓存
     * @author ztp
     * @date 2024/4/15 19:42
     * @param redisKey 需要删除的key
     * @param mapKey 如果为null,则删除整个map
     * @return java.lang.Boolean
    */
    public Boolean clearLocalCache(String redisKey, String mapKey) {
        LocalCacheKeyVo message = new LocalCacheKeyVo(2, redisKey, mapKey);
        LocalDataUtils.clearLocalData(message);
        return topic.publish(message) > 0;
    }


}
