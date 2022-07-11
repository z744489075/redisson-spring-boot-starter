package com.zengtengpeng.operation;

import com.zengtengpeng.func.RealData;
import com.zengtengpeng.func.RealDataList;
import com.zengtengpeng.func.RealDataMap;
import com.zengtengpeng.func.RealDataSet;
import com.zengtengpeng.properties.RedissonProperties;
import org.redisson.api.*;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 操作映射缓存集合
 */
public class RedissonCollectionCache {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedissonProperties redissonProperties;

    /**
     * 获取map集合
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return redissonClient.getMapCache(name);
    }
    /**
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMapCache<K, V> getMapCache(String name, RealDataMap realDataMap){
        return getMapCache(name,realDataMap,redissonProperties.getDataValidTime());
    }
    /**
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMapCache<K, V> getMapCache(String name, RealDataMap realDataMap,Long time) {
        RMapCache<Object, Object> map = redissonClient.getMapCache(name);
        if(map==null ||map.size()==0){
            Map objectObjectMap = realDataMap.get();
            setMapCacheValues(name,objectObjectMap,time);
        }
        return redissonClient.getMapCache(name);
    }

    /**
     * 获取map集合对应的key值
     *
     * @param name
     * @return
     */
    public <T> T getMapCacheValue(String name, String key) {
        return (T) getMapCache(name).get(key);
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapCacheValue(String name, String key, RealData realData) {
        return getMapCacheValue(name,key,realData,redissonProperties.getDataValidTime());
    }
    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapCacheValue(String name, String key, RealData realData,Long time) {
        RMapCache<Object, Object> mapCache = getMapCache(name);
        Object o = mapCache.get(key);
        if(o==null){
            o = realData.get();
            if(ObjectUtils.isEmpty(o)){
                mapCache.remove(key);
            }else {
                setMapCacheValue(name,key,o,time);
            }
        }
        return (T) o;
    }

    /**
     * 设置map集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setMapCacheValues(String name, Map data, Long time) {
        RMapCache map = redissonClient.getMapCache(name);

        if (time == null) {
            time= redissonProperties.getDataValidTime();
        }
        map.putAll(data,time, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置 map值
     *
     * @param name
     * @param time 缓存时间,单位毫秒 0永久缓存
     * @return
     */
    public void setMapCacheValue(String name, String key, Object value, Long time) {
        setMapCacheValue(name,key,value,time,0L);
    }

    /**
     * 设置 map值
     *
     * @param name
     * @param time 缓存时间,单位毫秒 0永久缓存
     * @return
     */
    public void setMapCacheValue(String name, String key, Object value, Long time,Long maxIdleTime) {
        RMapCache map = redissonClient.getMapCache(name);
        if (time == null) {
            time= redissonProperties.getDataValidTime();
        }
        map.put(key, value,time,TimeUnit.MILLISECONDS,maxIdleTime,TimeUnit.MILLISECONDS);
    }

    /**
     * 设置map集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setMapCacheValues(String name, Map data) {
        setMapCacheValues(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 设置map集合
     *
     * @param name
     * @return
     */
    public void setMapCacheValue(String name, String key, Object value) {
        setMapCacheValue(name, key, value, redissonProperties.getDataValidTime());
    }


    /**
     * 获取set集合
     *
     * @param name
     * @return
     */
    public <T> RSetCache<T> getSetCache(String name) {
        return redissonClient.getSetCache(name);
    }
    /**
     * 获取List集合 如果没有则通过实时数据获取
     *
     * @param name
     * @return
     */
    public <T> RSetCache<T> getSetCache(String name, RealDataSet realDataSet, Long time) {
        RSetCache<Object> set = getSetCache(name);
        if(set==null || set.size()==0){
            Set<Object> objects = realDataSet.get();
            setSetCacheValues(name,objects,time);
        }
        return getSetCache(name);
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> RSetCache<T> getSetCache(String name, RealDataSet realDataSet) {
        return getSetCache(name,realDataSet,redissonProperties.getDataValidTime());
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setSetCacheValues(String name, Set data, Long time) {
        RSetCache<Object> set = redissonClient.getSetCache(name);
        set.addAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            set.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            set.expire(Duration.ofMillis(time));
        }
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setSetCacheValue(String name, Object data, Long time) {
        RSetCache<Object> set = redissonClient.getSetCache(name);
        if (time == null) {
            time=redissonProperties.getDataValidTime();
        }
        set.add(data,time,TimeUnit.MILLISECONDS);
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setSetCacheValues(String name, Set data) {
        setSetCacheValues(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setSetValues(String name, Object data) {
        setSetCacheValue(name, data, redissonProperties.getDataValidTime());
    }

}
