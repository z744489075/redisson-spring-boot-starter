# redisson-spring-boot-starter 
目前有很多项目还在使用jedis的 `setNx` 充当分布式锁,然而这个锁是有问题的,redisson是java支持redis的redlock的`唯一`实现,
集成该项目后只需要极少的配置.就能够使用redisson的全部功能. 目前支持
`集群模式`,`云托管模式`,`单Redis节点模式`,`哨兵模式`,`主从模式` 配置. 支持 `可重入锁`,`公平锁`,`联锁`,`红锁`,`读写锁` 锁定模式


#### 介绍
1. 我们为什么需要`redisson`?

>`redisson`目前是官方唯一推荐的java版的分布式锁,他支持 `redlock`.具体请查看 [官方文档](https://redis.io/topics/distlock)

1. jedis为什么有问题? 

> 目前jedis是只支持单机的.

> jedis setNx 和设置过期时间是不同步的,在某些极端的情况下会发生死锁.导致程序崩溃.如果没有设置value,
线程1可能会释放线程2的锁 详情看下这篇 [博客](https://blog.csdn.net/u014677702/article/details/83308972)


#### 软件架构
1. [redisson](https://github.com/redisson/redisson) 
2. spring boot

#### 安装教程

1. 引入 pom.xml

```xml
<dependency>
    <groupId>com.zengtengpeng</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

#### 使用说明

1. 在  `application.properties` 增加

```
#单Redis节点模式
redisson.singleServerConfig.address=127.0.0.1:6379
```

2.在方法增加 `@Lock` 注解

```

//支持 spel 表达式 如果后面需要接字符串的话请用`+`连接. 字符串一定要打`单引号`
@Lock(keys = "#user.name+'locks'")
public String test(User user) {
    System.out.println("进来了test");
    return "test";
}

```

3. 完毕

# 进阶篇

#### @Lock 注解参数介绍

```
    /**
     * REENTRANT(可重入锁),FAIR(公平锁),MULTIPLE(联锁),REDLOCK(红锁),READ(读锁), WRITE(写锁), 
     * AUTO(自动模式,当参数只有一个.使用 REENTRANT 参数多个 MULTIPLE)
     */
    LockModel lockModel() default LockModel.AUTO;
    /**
     * 需要锁定的keys
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
```

#### 如何使用`redisson` 客户端实现自定义操作,只需要在spring 容器中注入redisson客户端就行,如下:

```
    @Autowired
    private RedissonClient redissonClient;
```

#### 集群模式配置(也可以使用yml写法)

##### 单例模式

>单机版redis
```
#单Redis节点模式
redisson.singleServerConfig.address=127.0.0.1:6379
```

##### 集群模式

>集群模式除了适用于Redis集群环境，也适用于任何云计算服务商提供的集群模式，例如AWS ElastiCache集群版、Azure Redis Cache和阿里云（Aliyun）的云数据库Redis版。
```
#集群模式
redisson.model=CLUSTER
#redis机器.一直累加下去
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

##### 云托管模式

>云托管模式适用于任何由云计算运营商提供的Redis云服务，包括亚马逊云的AWS ElastiCache、微软云的Azure Redis 缓存和阿里云（Aliyun）的云数据库Redis版

```
#云托管模式
redisson.model=REPLICATED
#redis机器.一直累加下去
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

##### 哨兵模式

```
redisson.model=SENTINEL
#主服务器的名称是哨兵进程中用来监测主从服务切换情况的。
redisson.multiple-server-config.master-name="mymaster"
#redis机器.一直累加下去
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

##### 主从模式

```
redisson.model=MASTERSLAVE
#第一台机器就是主库.其他的为从库
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

### 属性列表(基本都是官方参数.我将参数整合了下.分为 `公共参数`,`单例模式参数`,`集群模式参数`) [链接地址](readme/attr.md)




