package com.zengtengpeng.operation;

import com.zengtengpeng.func.RealData;
import com.zengtengpeng.func.RealDataList;
import com.zengtengpeng.func.RealDataMap;
import com.zengtengpeng.func.RealDataSet;
import com.zengtengpeng.properties.RedissonProperties;
import org.redisson.api.*;

import javax.annotation.Resource;
import java.time.Duration;
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
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMap<K, V> getMap(String name, RealDataMap realDataMap){
        return getMap(name,realDataMap,redissonProperties.getDataValidTime());
    }
    /**
     * 获取map集合,如果没有则通过实时数据
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> RMap<K, V> getMap(String name, RealDataMap realDataMap,Long time) {
        RMap<Object, Object> map = redissonClient.getMap(name);
        if(map==null ||map.size()==0){
            Map objectObjectMap = realDataMap.get();
            setMapValues(name,objectObjectMap,time);
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
    public void setMapValue(String name, String key, Object value, Long time) {
        RMap map = redissonClient.getMap(name);
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
     * 获取List集合 如果没有则通过实时数据获取
     *
     * @param name
     * @return
     */
    public <T> RList<T> getList(String name,RealDataList realDataList, Long time) {
        RList<Object> list = getList(name);
        if(list==null || list.size()==0){
            List objects = realDataList.get();
            setListValues(name,objects,time);
        }
        return getList(name);
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> RList<T> getList(String name, RealDataList realDataList) {
        return getList(name,realDataList,redissonProperties.getDataValidTime());
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
    public void setListValue(String name, Object data, Long time) {
        RList list = redissonClient.getList(name);
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
     * 获取List集合 如果没有则通过实时数据获取
     *
     * @param name
     * @return
     */
    public <T> RSet<T> getSet(String name, RealDataSet realDataSet, Long time) {
        RSet<Object> set = getSet(name);
        if(set==null || set.size()==0){
            Set<Object> objects = realDataSet.get();
            setSetValues(name,objects,time);
        }
        return getSet(name);
    }

    /**
     * 获取List集合对应的index值
     *
     * @param name
     * @return
     */
    public <T> RSet<T> getSet(String name, RealDataSet realDataSet) {
        return getSet(name,realDataSet,redissonProperties.getDataValidTime());
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
    public void setSetValue(String name, Object data, Long time) {
        RSet set = redissonClient.getSet(name);
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
