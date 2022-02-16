package com.zengtengpeng.func;

import java.util.List;
import java.util.Map;

/**
 *  获取集合实时数据
 *
 * @author ztp
 * @return
 * @date 2022/2/16 15:10
 */
@FunctionalInterface
public interface RealDataMap<K,V> {
    Map<K,V> get();
}
