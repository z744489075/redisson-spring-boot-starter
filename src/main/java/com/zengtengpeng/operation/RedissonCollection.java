package com.zengtengpeng.operation;

import com.zengtengpeng.func.RealData;
import com.zengtengpeng.properties.RedissonProperties;
import org.redisson.api.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 操作集合
 */
public class RedissonCollection {

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
    public <K, V> RMap<K, V> getMap(String name) {
        return redissonClient.getMap(name);
    }

    /**
     * 获取map集合对应的key值
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key) {
        return (T) getMap(name).get(key);
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key, RealData realData) {
        Object o = getMap(name).get(key);
        if(o==null){
            o = realData.get();
            setMapValue(name,key,o);
        }
        return (T) o;
    }
    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key, RealData realData,Long time) {
        Object o = getMap(name).get(key);
        if(o==null){
            o = realData.get();
            setMapValue(name,key,o,time);
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
    public void setMapValues(String name, Map data, Long time) {
        RMap map = redissonClient.getMap(name);
        map.putAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            map.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            map.expire(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置 map值
     *
     * @param name
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setMapValue(String name, String key, Object value, Long time) {
        RMap map = redissonClient.getMap(name);
        map.put(key, value);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            map.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            map.expire(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置map集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setMapValues(String name, Map data) {
        setMapValues(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 设置map集合
     *
     * @param name
     * @return
     */
    public void setMapValue(String name, String key, Object value) {
        setMapValue(name, key, value, redissonProperties.getDataValidTime());
    }

    /**
     * 获取List集合
     *
     * @param name
     * @return
     */
    public <T> RList<T> getList(String name) {
        return redissonClient.getList(name);
    }
    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> T getListValue(String name,Integer index) {
        return (T) getList(name).get(index);
    }
    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> T getListValue(String name,Integer index,RealData realData) {
        Object o = getList(name).get(index);
        if(o==null){
            o = realData.get();
            setListValue(name,o);
        }
        return (T) o;
    }
    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> T getListValue(String name,Integer index,RealData realData,Long time) {
        Object o = getList(name).get(index);
        if(o==null){
            o = realData.get();
            setListValue(name,o,time);
        }
        return (T) o;
    }

    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setListValues(String name, List data, Long time) {
        RList list = redissonClient.getList(name);
        list.addAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            list.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            list.expire(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setListValue(String name, Object data, Long time) {
        RList list = redissonClient.getList(name);
        list.add(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            list.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            list.expire(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setListValues(String name, List data) {
        setListValues(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setListValue(String name, Object data) {
        setListValue(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 获取set集合
     *
     * @param name
     * @return
     */
    public <T> RSet<T> getSet(String name) {
        return redissonClient.getSet(name);
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public void setSetValues(String name, Set data, Long time) {
        RSet set = redissonClient.getSet(name);
        set.addAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            set.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            set.expire(time, TimeUnit.MILLISECONDS);
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
    public void setSetValue(String name, Object data, Long time) {
        RSet set = redissonClient.getSet(name);
        set.add(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            set.expire(dataValidTime, TimeUnit.MILLISECONDS);
        } else if (time != -1) {
            set.expire(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setSetValues(String name, Set data) {
        setSetValues(name, data, redissonProperties.getDataValidTime());
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @return
     */
    public void setSetValues(String name, Object data) {
        setSetValue(name, data, redissonProperties.getDataValidTime());
    }

}
