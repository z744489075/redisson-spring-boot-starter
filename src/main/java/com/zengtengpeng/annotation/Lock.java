package com.zengtengpeng.annotation;

import com.zengtengpeng.enums.LockModel;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

    /**
     * 锁的模式:如果不设置,自动模式,当参数只有一个.使用 REENTRANT 参数多个 MULTIPLE
     */
    LockModel lockModel() default LockModel.AUTO;

    /**
     * 如果keys有多个,如果不设置,则使用 联锁
     * @return
     */
    String[] keys() default {};

    /**
     * 锁超时时间,默认30000毫秒(可在配置文件全局设置)
     * @return
     */
    long lockWatchdogTimeout() default 0;

    /**
     * 等待加锁超时时间,默认10000毫秒 -1 则表示一直等待(可在配置文件全局设置)
     * @return
     */
    long attemptTimeout() default 0;


}
