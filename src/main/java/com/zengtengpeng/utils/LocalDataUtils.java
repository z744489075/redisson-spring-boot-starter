package com.zengtengpeng.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.zengtengpeng.bean.LocalCacheKeyVo;
import com.zengtengpeng.operation.RedissonCollectionLocalCache;
import com.zengtengpeng.operation.RedissonObjectLocalCache;

import java.util.Map;

public class LocalDataUtils {


    public static void clearLocalData(LocalCacheKeyVo localCacheKeyVo) {
        String mapKey = localCacheKeyVo.getMapKey();

        Map<String, Cache<String, Object>> cacheMap = RedissonObjectLocalCache.cacheMap;
        if(localCacheKeyVo.getType()==2){
            cacheMap= RedissonCollectionLocalCache.cacheMap;
        }

        Cache<String, Object> stringObjectCache = cacheMap.get(localCacheKeyVo.getLocalCacheKey());
        if(stringObjectCache!=null) {
            if (mapKey == null) {
                stringObjectCache.invalidateAll();
            } else {
                stringObjectCache.invalidate(mapKey);
            }
        }
    }
}
