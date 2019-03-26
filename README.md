# redisson-spring-boot-starter
目前有很多项目还在使用jedis的 `setNx` 充当分布式锁,然而这个锁是有问题的,redisson是java支持redis的分布式锁的`唯一`实现,
官方目前只有java web版本,配置起来很麻烦.集成该项目后只需要极少的配置.就能过使用redisson的全部功能. 目前支持
`集群模式`,`云托管模式`,`单Redis节点模式`,`哨兵模式`,`主从模式` 配置. 支持 `可重入锁`,`公平锁`,`联锁`,`红锁`,`读写锁` 锁定模式

#### 介绍
1. 我们为什么需要`redisson`?

>`redisson`目前是官方唯一推荐的java版的分布式锁,他支持 `redlock`.具体请查看 [官方文档](https://redis.io/topics/distlock)

1. jedis为什么锁不住? 

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
    <version>1.0.0</version>
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

//支持 spel 表达式
@Lock(keys = "#user.name")
public String test(User user) {
    System.out.println("进来了test");
    return "test";
}

```

3. 完毕

# 进阶篇

#### 如何使用`redisson` 客户端,只需要在spring 容器中注入客户端就行,如下:

```
    @Autowired
    private RedissonClient redissonClient;
```

#### 模式配置(也可以使用yml写法)

#####单例模式
>单机版redis
```
#单Redis节点模式
redisson.singleServerConfig.address=127.0.0.1:6379
```
#####集群模式
>集群模式除了适用于Redis集群环境，也适用于任何云计算服务商提供的集群模式，例如AWS ElastiCache集群版、Azure Redis Cache和阿里云（Aliyun）的云数据库Redis版。
```
#集群模式
redisson.model=CLUSTER
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```
#####云托管模式
>云托管模式适用于任何由云计算运营商提供的Redis云服务，包括亚马逊云的AWS ElastiCache、微软云的Azure Redis 缓存和阿里云（Aliyun）的云数据库Redis版

```
#云托管模式
redisson.model=REPLICATED
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

#####哨兵模式

```
redisson.model=SENTINEL
#主服务器的名称是哨兵进程中用来监测主从服务切换情况的。
redisson.multiple-server-config.master-name="mymaster"
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

#####主从模式

```
redisson.model=MASTERSLAVE
#第一台机器就是主库.其他的为从库
redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379
redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380
redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381
```

###属性列表(基本都是官方参数.我将参数整合了下.分为 `公共参数`,`单例模式参数`,`集群模式参数`)

> 1.公共参数

属性名 | 默认值|备注
---|    ---    |---
redisson.password | |用于节点身份验证的密码。 |
redisson.model |SINGLE | 集群模式:SINGLE(单例),SENTINEL(哨兵),MASTERSLAVE(主从),CLUSTER(集群),REPLICATED(云托管)
redisson.codec | org.redisson.codec.JsonJacksonCodec | Redisson的对象编码类是用于将对象进行序列化和反序列化，以实现对该对象在Redis里的读取和存储 | 
redisson.threads | 当前处理核数量 * 2 |这个线程池数量被所有RTopic对象监听器，RRemoteService调用者和RExecutorService任务共同共享。 | 
redisson.nettyThreads | 当前处理核数量 * 2 | 这个线程池数量是在一个Redisson实例内，被其创建的所有分布式数据类型和服务，以及底层客户端所一同共享的线程池里保存的线程数量。| 
redisson.transportMode | NIO | TransportMode.NIO,</br>TransportMode.EPOLL - 需要依赖里有netty-transport-native-epoll包（Linux）</br> TransportMode.KQUEUE - 需要依赖里有 netty-transport-native-kqueue包（macOS）| 
redisson.idleConnectionTimeout | 10000 | 如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒| 
redisson.connectTimeout |10000 |同任何节点建立连接时的等待超时。时间单位是毫秒。 | 
redisson.timeout | 3000|等待节点回复命令的时间。该时间从命令发送成功时开始计时。 | 
redisson.retryAttempts |3 |如果尝试达到 retryAttempts（命令失败重试次数） 仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，则开始启用 timeout（命令等待超时） 计时。 | 
redisson.retryInterval |1500 |在一条命令发送失败以后，等待重试发送的时间间隔。时间单位是毫秒。 | 
redisson.subscriptionsPerConnection |5 | 每个连接的最大订阅数量。 | 
redisson.clientName | |在Redis节点里显示的客户端名称。 | 
redisson.sslEnableEndpointIdentification |true |开启SSL终端识别能力。 | 
redisson.sslProvider | JDK|确定采用哪种方式（JDK或OPENSSL）来实现SSL连接。 | 
redisson.sslTruststore | | 指定SSL信任证书库的路径。| 
redisson.sslTruststorePassword | |指定SSL信任证书库的密码。 | 
redisson.sslKeystore | |指定SSL钥匙库的路径。 | 
redisson.sslKeystorePassword | |指定SSL钥匙库的密码。 | 
redisson.lockWatchdogTimeout | 30000|监控锁的看门狗超时时间单位为毫秒。该参数只适用于分布式锁的加锁请求中未明确使用leaseTimeout参数的情况。如果该看门口未使用lockWatchdogTimeout去重新调整一个分布式锁的lockWatchdogTimeout超时，那么这个锁将变为失效状态。这个参数可以用来避免由Redisson客户端节点宕机或其他原因造成死锁的情况。 | 
redisson.keepPubSubOrder | true|通过该参数来修改是否按订阅发布消息的接收顺序出来消息，如果选否将对消息实行并行处理，该参数只适用于订阅发布消息的情况。 | 
redisson.lockModel | | 锁的模式.如果不设置,单个key默认可重入锁多个key默认联锁| 
redisson.attemptTimeout |10000L | 等待获取锁超时时间,-1则是一直等待 | 

>2. 单例模式参数

属性名 | 默认值|备注
---|---|---
redisson.singleServerConfig.address | | 服务器地址,必填ip:port
redisson.singleServerConfig.subscriptionConnectionMinimumIdleSize |1 | 用于发布和订阅连接的最小保持连接数（长连接）。Redisson内部经常通过发布和订阅来实现许多功能。长期保持一定数量的发布订阅连接是必须的。
redisson.singleServerConfig.subscriptionConnectionPoolSize | 50| 用于发布和订阅连接的连接池最大容量。连接池的连接数量自动弹性伸缩。
redisson.singleServerConfig.connectionMinimumIdleSize |32 | 最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时写入反应速度。
redisson.singleServerConfig.connectionPoolSize |64 | 连接池最大容量。连接池的连接数量自动弹性伸缩。
redisson.singleServerConfig.database | 0| 尝试连接的数据库编号。
redisson.singleServerConfig.dnsMonitoringInterval |5000 | 用来指定检查节点DNS变化的时间间隔。使用的时候应该确保JVM里的DNS数据的缓存时间保持在足够低的范围才有意义。用-1来禁用该功能。

>3. 集群模式


属性名 | 默认值|备注
---|---|---
redisson.multiple-server-config.node-addresses | | 服务器节点地址.必填 <br/>redisson.multiple-server-config.node-addresses[0]=127.0.0.1:6379<br/>redisson.multiple-server-config.node-addresses[1]=127.0.0.1:6380<br/>redisson.multiple-server-config.node-addresses[2]=127.0.0.1:6381<br/>
redisson.multiple-server-config.loadBalancer |org.redisson.connection.balancer.RoundRobinLoadBalancer | 在多Redis服务节点的环境里，可以选用以下几种负载均衡方式选择一个节点：<br/> org.redisson.connection.balancer.WeightedRoundRobinBalancer - 权重轮询调度算法<br/> org.redisson.connection.balancer.RoundRobinLoadBalancer - 轮询调度算法<br/> org.redisson.connection.balancer.RandomLoadBalancer - 随机调度算法
redisson.multiple-server-config.slaveConnectionMinimumIdleSize |32 | 多从节点的环境里，每个 从服务节点里用于普通操作（非 发布和订阅）的最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时读取反映速度。
redisson.multiple-server-config.slaveConnectionPoolSize |64 | 多从节点的环境里，每个 从服务节点里用于普通操作（非 发布和订阅）连接的连接池最大容量。连接池的连接数量自动弹性伸缩。
redisson.multiple-server-config.masterConnectionMinimumIdleSize |32 | 多节点的环境里，每个 主节点的最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时写入反应速度。
redisson.multiple-server-config.masterConnectionPoolSize | 64| 多主节点的环境里，每个 主节点的连接池最大容量。连接池的连接数量自动弹性伸缩。
redisson.multiple-server-config.readMode | SLAVE| 设置读取操作选择节点的模式。 可用值为： SLAVE - 只在从服务节点里读取。 MASTER - 只在主服务节点里读取。 MASTER_SLAVE - 在主从服务节点里都可以读取。
redisson.multiple-server-config.subscriptionMode |SLAVE | 设置订阅操作选择节点的模式。 可用值为： SLAVE - 只在从服务节点里订阅。 MASTER - 只在主服务节点里订阅。
redisson.multiple-server-config.subscriptionConnectionMinimumIdleSize |1 |用于发布和订阅连接的最小保持连接数（长连接）。Redisson内部经常通过发布和订阅来实现许多功能。长期保持一定数量的发布订阅连接是必须的。 redisson.multiple-server-config.subscriptionConnectionPoolSize |50 | 用于发布和订阅连接的连接池最大容量。连接池的连接数量自动弹性伸缩。connectionMinimumIdleSize（最小空闲连接数）
redisson.multiple-server-config.dnsMonitoringInterval |5000 | 监测DNS的变化情况的时间间隔。
redisson.multiple-server-config.scanInterval | 1000 | (集群,哨兵,云托管模特特有) 对Redis集群节点状态扫描的时间间隔。单位是毫秒。
redisson.multiple-server-config.database | 0 | (哨兵模式,云托管,主从模式特有)尝试连接的数据库编号。
redisson.multiple-server-config.masterName | | (哨兵模式特有)主服务器的名称是哨兵进程中用来监测主从服务切换情况的。

