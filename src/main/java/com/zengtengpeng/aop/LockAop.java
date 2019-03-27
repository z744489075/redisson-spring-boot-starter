package com.zengtengpeng.aop;

import com.zengtengpeng.annotation.Lock;
import com.zengtengpeng.enums.LockModel;
import com.zengtengpeng.excepiton.LockException;
import com.zengtengpeng.properties.RedissonProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.TimeUnit;


/**
 * 分布式锁aop
 */
@Aspect
public class LockAop {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedissonProperties redissonProperties;
    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(lock)")
    public void controllerAspect(Lock lock) {
    }

    /**
     * 通过spring Spel 获取参数
     * @param key               定义的key值 以#开头 例如:#user
     * @param parameterNames    形参
     * @param values             形参值
     * @return
     */
    public String getVauleBySpel(String key, String[] parameterNames, Object[] values) {

        if(!key.contains("#")){
            String s = "redisson:lock:" + key;
            log.info("没有使用spel表达式value->{}",s);
            return s;
        }
        //spel解析器
        ExpressionParser parser = new SpelExpressionParser();
        //spel上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], values[i]);
        }
        Expression expression = parser.parseExpression(key);
        Object value = expression.getValue(context);
        if(value!=null){
            String s = "redisson:lock:" + value.toString();
            log.info("spel表达式key={},value={}",key,s);
            return s;
        }
        return "redisson:lock";
    }

    @Around("controllerAspect(lock)")
    public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint, Lock lock) throws Throwable {
        String[] keys = lock.keys();
        if (keys.length == 0) {
            throw new RuntimeException("keys不能为空");
        }
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(((MethodSignature) proceedingJoinPoint.getSignature()).getMethod());
        Object[] args = proceedingJoinPoint.getArgs();

        long attemptTimeout = lock.attemptTimeout();
        if (attemptTimeout == 0) {
            attemptTimeout = redissonProperties.getAttemptTimeout();
        }
        long lockWatchdogTimeout = lock.lockWatchdogTimeout();
        if (lockWatchdogTimeout == 0) {
            lockWatchdogTimeout = redissonProperties.getLockWatchdogTimeout();
        }
        LockModel lockModel = lock.lockModel();
        if (lockModel.equals(LockModel.AUTO)) {
            LockModel lockModel1 = redissonProperties.getLockModel();
            if (lockModel1 != null) {
                lockModel = lockModel1;
            } else if (keys.length > 1) {
                lockModel = LockModel.MULTIPLE;
            } else {
                lockModel = LockModel.REENTRANT;
            }
        }
        if (!lockModel.equals(LockModel.MULTIPLE) && !lockModel.equals(LockModel.REDLOCK) && keys.length > 1) {
            throw new RuntimeException("参数有多个,锁模式为->" + lockModel.name() + ".无法锁定");
        }
        log.info("锁模式->{},等待锁定时间->{}秒.锁定最长时间->{}秒",lockModel.name(),attemptTimeout/1000,lockWatchdogTimeout/1000);
        boolean res = false;
        RLock rLock = null;
        //一直等待加锁.
        switch (lockModel) {
            case FAIR:
                rLock = redissonClient.getFairLock(getVauleBySpel(keys[0],parameterNames,args));
                break;
            case REDLOCK:
                RLock[] locks=new RLock[keys.length];
                int index=0;
                for (String key : keys) {
                    locks[index++]=redissonClient.getLock(getVauleBySpel(key,parameterNames,args));
                }
                rLock = new RedissonRedLock(locks);
                break;
            case MULTIPLE:
                RLock[] locks1=new RLock[keys.length];
                int index1=0;
                for (String key : keys) {
                    locks1[index1++]=redissonClient.getLock(getVauleBySpel(key,parameterNames,args));
                }
                rLock = new RedissonMultiLock(locks1);
                break;
            case REENTRANT:
                rLock= redissonClient.getLock(getVauleBySpel(keys[0],parameterNames,args));
                break;
            case READ:
                RReadWriteLock rwlock = redissonClient.getReadWriteLock(getVauleBySpel(keys[0],parameterNames,args));
                rLock = rwlock.readLock();
                break;
            case WRITE:
                RReadWriteLock rwlock1 = redissonClient.getReadWriteLock(getVauleBySpel(keys[0],parameterNames,args));
                rLock = rwlock1.writeLock();
                break;
        }

        //执行aop
        if(rLock!=null) {
            try {
                if (attemptTimeout == -1) {
                    res = true;
                    //一直等待加锁
                    rLock.lock(lockWatchdogTimeout, TimeUnit.MILLISECONDS);
                } else {
                    res = rLock.tryLock(attemptTimeout, lockWatchdogTimeout, TimeUnit.MILLISECONDS);
                }
                if (res) {
                    Object obj = proceedingJoinPoint.proceed();
                    return obj;
                }else{
                    throw new LockException("获取锁失败");
                }
            } finally {
            	if (res) {
            		rLock.unlock();
            	}
            }
        }
        throw new LockException("获取锁失败");
    }
}
