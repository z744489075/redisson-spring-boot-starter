>2022-02-17 

增加 ```com.zengtengpeng.codec.MyJsonJacksonCodec``` 解决 LocalDateTime
LocalDate LocalTime 序列化问题

> 2022-02-16:1.0.15 增加 RedissonCollectionCache 类

> 2022-02-16:1.0.14 redissonObject,redissonCollection 增加新的方法
    
```java
        //获取Map ,如果redis没有,则从接口函数中获取存入map 并返回
        redissonCollection.getMap("map", () -> {
            Map<String,String> map=new HashMap<>();
            map.put("123","456");
            map.put("789","111");
            return map;
        });
        //获取List ,如果redis没有,则从接口函数中获取存入List 并返回
        redissonCollection.getList("list",()->{
            List<String> list=new ArrayList<>();
            list.add("123");
            list.add("456");
            return list;
        });

        //获取Set ,如果redis没有,则从接口函数中获取存入Set 并返回
        redissonCollection.getSet("set",()->{
            Set<String> set=new HashSet<>();
            set.add("1111");
            set.add("2222");
            return set;
        });

        //如果缓存有值从缓存里面读, 否则从接口函数读实时数据存入redis
        Object test1 = redissonCollection.getMapValue("test", "444", () -> {
            return 1234;
        }, 2000000L);
```


>2019-10-16:1.0.4更新

修复@Transactional与@lock同时存在时执行顺序问题

>2019-07-09:1.0.3更新

现在spel支持LIST集合,以及数组了,默认情况将使用红锁进行多个key锁定.(每个元素将是一个key.有几个元素就会有几把锁)

>2019-04-02:1.0.2更新

1.增加spring cache 整合

2.增加session集群

3.增加消息队列

4.增加对象存储 
