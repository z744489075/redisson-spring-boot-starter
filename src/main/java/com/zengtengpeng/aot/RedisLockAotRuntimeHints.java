package com.zengtengpeng.aot;

import com.zengtengpeng.aop.LockAop;
import com.zengtengpeng.aop.MQAop;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.DecoratingProxy;

import java.util.stream.Stream;


/**
 * 锁注入 AOT
 *
 *
 * @author ztp
 * @date 2023/7/21 16:33
 */
public class RedisLockAotRuntimeHints implements RuntimeHintsRegistrar {


    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Stream.of(
                LockAop.class,
                MQAop.class

        ).forEach(x -> hints.reflection().registerType(x, MemberCategory.values()));

        hints.proxies().registerJdkProxy(LockAop.class,
                MQAop.class,
                SpringProxy.class,
                DecoratingProxy.class,
                Advised.class
        );
    }



}