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
线程1可能会释放线程2的锁


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

2.在方法增加 `@Lock` 注解 [lock参数介绍](readme/lock.md)

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

#### 如何使用`redisson` 客户端实现自定义操作,只需要在spring 容器中注入redisson客户端就行,如下:

```
    @Autowired
    private RedissonClient redissonClient;
```

#### 集群模式配置(也可以使用yml写法) [链接地址](readme/mode.md)


### 属性列表(基本都是官方参数.我将参数整合了下.分为 `公共参数`,`单例模式参数`,`集群模式参数`) [链接地址](readme/attr.md)




