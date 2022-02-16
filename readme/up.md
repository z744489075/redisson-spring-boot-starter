
> 2022-02-16:1.0.11 redissonObject,redissonCollection 增加新的方法
    
//如果缓存有值从缓存里面读, 否则从接口函数读实时数据存入redis后返回新值
redissonObject.getValue("object1",()->"获取值逻辑",200213213L);

//如果缓存有值从缓存里面读, 否则从接口函数读实时数据存入redis
Object test1 = redissonCollection.getMapValue("test", "444", () -> {
return "获取值";
}, 2000000L);


>2019-10-16:1.0.4更新

修复@Transactional与@lock同时存在时执行顺序问题

>2019-07-09:1.0.3更新

现在spel支持LIST集合,以及数组了,默认情况将使用红锁进行多个key锁定.(每个元素将是一个key.有几个元素就会有几把锁)

>2019-04-02:1.0.2更新

1.增加spring cache 整合

2.增加session集群

3.增加消息队列

4.增加对象存储 
