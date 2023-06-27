package com.zengtengpeng.operation;

import com.zengtengpeng.func.*;
import com.zengtengpeng.properties.RedissonProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.redisson.api.*;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 操作集合
 */
public class RedissonCollection {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
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
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMap<K, V> getMap(String name, RealDataMap<K, V> realDataMap) {
        return getMap(name, realDataMap, redissonProperties.getDataValidTime());
    }

    /**
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMap<K, V> getMap(String name, RealDataMap<K, V> realDataMap, Long time) {
        RMap<Object, Object> map = redissonClient.getMap(name);
        if (map == null || map.size() == 0) {
            Map objectObjectMap = realDataMap.get();
            setMapValues(name, objectObjectMap, time);
        }
        return redissonClient.getMap(name);
    }

    /**
     * 获取map集合对应的key值
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key) {
        RMap<Object, T> map = getMap(name);
        return map.get(key);
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key, RealData<T> realData) {

        return getMapValue(name, key, realData, redissonProperties.getDataValidTime());
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key, RealData<T> realData, Long time) {
        RMap<Object, T> map = getMap(name);
        T o = map.get(key);
        if (o == null) {
            o = realData.get();
            if (ObjectUtils.isEmpty(o)) {
                map.remove(key);
            } else {
                setMapValue(name, key, o, time);
            }
        }
        return o;
    }

    /**
     * 先从map集合获取数据,如果没有则从接口获取
     *
     * @param name
     * @return
     */
    public <T> T getMapValue(String name, String key, RealData<T> realData, DataCache<T> dataCache, Long time) {
        RMap<Object, T> map = getMap(name);
        T o = map.get(key);
        if (o == null) {
            o = realData.get();
            if (ObjectUtils.isEmpty(o)) {
                map.remove(key);
            } else {
                Boolean cache = dataCache.isCache(o);
                if (cache) {
                    setMapValue(name, key, o, time);
                }
            }
        }
        return o;
    }

    /**
     * 设置map集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public <K,V> void setMapValues(String name, Map<K,V> data, Long time) {
        RMap<K,V> map = redissonClient.getMap(name);
        map.putAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            map.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            map.expire(Duration.ofMillis(time));
        }
    }

    /**
     * 设置 map值
     *
     * @param name
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public <T> void setMapValue(String name, String key, T value, Long time) {
        RMap<String,T> map = redissonClient.getMap(name);
        map.put(key, value);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            map.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            map.expire(Duration.ofMillis(time));
        }
    }

    /**
     * 设置map集合
     *
     * @param name
     * @param data
     * @return
     */
    public <K,V> void setMapValues(String name, Map<K,V> data) {
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
     * 获取List集合 如果没有则通过实时数据获取
     *
     * @param name
     * @return
     */
    public <T> RList<T> getList(String name, RealDataList<T> realDataList, Long time) {
        RList<Object> list = getList(name);
        if (list == null || list.size() == 0) {
            List<T> objects = realDataList.get();
            setListValues(name, objects, time);
        }
        return getList(name);
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> RList<T> getList(String name, RealDataList<T> realDataList) {
        return getList(name, realDataList, redissonProperties.getDataValidTime());
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> T getListValue(String name, Integer index) {
        RList<T> list = getList(name);
        return list.get(index);
    }


    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public <T> void setListValues(String name, List<T> data, Long time) {
        RList<T> list = redissonClient.getList(name);
        list.addAll(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            list.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            list.expire(Duration.ofMillis(time));
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
    public <T> void setListValue(String name, T data, Long time) {
        RList<T> list = redissonClient.getList(name);
        list.add(data);
        Long dataValidTime = redissonProperties.getDataValidTime();
        if (time == null) {
            list.expire(Duration.ofMillis(dataValidTime));
        } else if (time != -1) {
            list.expire(Duration.ofMillis(time));
        }
    }

    /**
     * 设置List集合
     *
     * @param name
     * @param data
     * @return
     */
    public <T> void setListValues(String name, List<T> data) {
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
     * 获取List集合 如果没有则通过实时数据获取
     *
     * @param name
     * @return
     */
    public <T> RSet<T> getSet(String name, RealDataSet<T> realDataSet, Long time) {
        RSet<Object> set = getSet(name);
        if (set == null || set.size() == 0) {
            Set<T> objects = realDataSet.get();
            setSetValues(name, objects, time);
        }
        return getSet(name);
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> RSet<T> getSet(String name, RealDataSet<T> realDataSet) {
        return getSet(name, realDataSet, redissonProperties.getDataValidTime());
    }

    /**
     * 设置set集合
     *
     * @param name
     * @param data
     * @param time 缓存时间,单位毫秒 -1永久缓存
     * @return
     */
    public <T> void setSetValues(String name, Set<T> data, Long time) {
        RSet<T> set = redissonClient.getSet(name);
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
    public <T> void setSetValue(String name, T data, Long time) {
        RSet<T> set = redissonClient.getSet(name);
        set.add(data);
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
     * @return
     */
    public <T> void setSetValues(String name, Set<T> data) {
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
