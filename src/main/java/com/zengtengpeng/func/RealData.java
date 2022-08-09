package com.zengtengpeng.func;

/**
 *  获取对象实时数据
 *
 * @author ztp
 * @return
 * @date 2022/2/16 15:10
 */
@FunctionalInterface
public interface RealData<T> {
    T get();
}
