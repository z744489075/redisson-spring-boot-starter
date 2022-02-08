
package com.zengtengpeng.annotation;

import org.redisson.spring.session.config.RedissonHttpSessionConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.MapSession;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RedissonHttpSessionConfiguration.class)
@Configuration
public @interface EnableRedissonHttpSession {

    int maxInactiveIntervalInSeconds() default MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;

    String keyPrefix() default "";

}
