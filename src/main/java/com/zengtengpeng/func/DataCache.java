package com.zengtengpeng.func;

/**
 *  是否缓存
 *
 * @author ztp
 * @return
 * @date 2022/2/16 15:10
 */
@FunctionalInterface
public interface DataCache<T> {
    /**
     * 是否缓存数据
     * @author ztp
     * @date 2022/8/9 14:48
     * @param t RealData 的值
     * @return java.lang.Boolean true 缓存 false:就算有值也不缓存
    */
    Boolean isCache(T t);
}
