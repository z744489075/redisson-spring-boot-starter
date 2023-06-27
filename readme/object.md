```java

@Controller
public class ObjectController {

    @Autowired
    private RedissonObject redissonObject;

    /**
     * 设置值
     * @param user
     * @param request
     * @param response
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/object1")
    @ResponseBody
    public String object1(User user, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        User user1 = new User();
        user1.setName("test");
        user1.setAge("123");
        redissonObject.setValue("object1", user1,-1L);
        
        return "";
    }

    /**
     * 获取值
     * @param user
     * @param request
     * @param response
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/object2")
    @ResponseBody
    public Object object2(User user, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        //如果缓存有值从缓存里面读, 否则从接口函数读实时数据存入redis
        redissonObject.getValue("object1",()->"获取值逻辑",200213213L);
        
        return redissonObject.getValue("object1");
    }

    /**
     * 如果对象不存在则设置,否则不设置
     * @param user
     * @param request
     * @param response
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/object3")
    @ResponseBody
    public String object3(User user, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        return redissonObject.trySetValue("object1","object1-2")+"";
    }
}
```
