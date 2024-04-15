package com.zengtengpeng.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zengtengpeng.operation.RedissonObjectMultiLocalCache;
import com.zengtengpeng.properties.RedissonProperties;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(value = RedissonProperties.class)
@ConditionalOnClass(RedissonProperties.class)
public class CaffeineConfig {

    @Autowired
    private RedissonProperties redissonProperties;

    @Autowired
    private RedissonClient redissonClient;

    //注册主题.当接收到信号,移除本地缓存
    public static String localCacheKey = "objectLocalCache";

    Logger logger = LoggerFactory.getLogger(CaffeineConfig.class);

    @Bean
    public RTopic topic(){
        return redissonClient.getTopic(localCacheKey);
    }

    /**
     * 本地缓存
    */
    @Bean
    public Cache<String,Object> cache(RTopic topic){
        Cache<String, Object> build = Caffeine.newBuilder()
                //设置过期时间
                .expireAfterWrite(redissonProperties.getLocalCacheTime(), TimeUnit.MILLISECONDS)
                //初始容量为100
                .initialCapacity(redissonProperties.getLocalInitSize())
                //最大容量为200
                .maximumSize(redissonProperties.getLocalMaxSize())
                .build();

        topic.addListener(LocalCacheKeyVo.class, (channel, key) -> {
            String localCacheKey = key.getLocalCacheKey();
            logger.info("开始清理localCacheKey:{}", key);
            if(ObjectUtils.isEmpty(localCacheKey)){
                build.invalidate(key.getName());
            }else {
                Map<String, Cache<String, Object>> cacheMap = RedissonObjectMultiLocalCache.cacheMap;
                Cache<String, Object> stringObjectCache = cacheMap.get(localCacheKey);
                if(stringObjectCache != null){
                    stringObjectCache.invalidate(key.getName());
                }
            }
        });
        return build;
    }

}
