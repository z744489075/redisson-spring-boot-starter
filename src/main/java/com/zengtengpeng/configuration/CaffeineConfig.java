package com.zengtengpeng.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.zengtengpeng.bean.LocalCacheKeyVo;
import com.zengtengpeng.operation.RedissonCollectionLocalCache;
import com.zengtengpeng.operation.RedissonObjectLocalCache;
import com.zengtengpeng.properties.RedissonProperties;
import com.zengtengpeng.utils.LocalDataUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
        RTopic topic = redissonClient.getTopic(localCacheKey);

        topic.addListener(LocalCacheKeyVo.class, (channel, localCacheKeyVo) -> {
            logger.info("开始清理localCacheKey:{}", localCacheKeyVo);
            LocalDataUtils.clearLocalData(localCacheKeyVo);
        });

        return topic;
    }



}
