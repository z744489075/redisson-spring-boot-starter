package com.zengtengpeng.func;

import java.util.List;

/**
 *  获取集合实时数据
 *
 * @author ztp
 * @return
 * @date 2022/2/16 15:10
 */
@FunctionalInterface
public interface RealDataList<T> {
    List<T> get();
}
