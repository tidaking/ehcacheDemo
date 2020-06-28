package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;


/*
*
* 	读：
		1.先读缓存
		2.没有命中，则回源查询
		3.写入缓存
		4.返回响应

	更新：
		1.先写入SoR
        2.写入成功后，将缓存数据删除或者过期，下次读取时再加载缓存
* */
@SpringBootTest
@Slf4j
class CacheAsideTest {

    @Autowired
    private TestServiceImpl testService;


    @Autowired
    @Qualifier("testCache")
    private Cache<String, String> testCache;

    @Autowired
    @Qualifier("testCacheManager")
    private CacheManager testCacheManager;


    /*
    * 读：
		1.先读缓存
		2.没有命中，则回源查询
		3.写入缓存
		4.返回响应
    * */
    @Test
    public void readDemo() throws InterruptedException {
        String key = "robinKey";
        String value = "robinValue";
        testService.put(key, value);//先写SOR
        testCache.put(key, value);//再写缓存

        Thread.currentThread().sleep(6 * 1000L);//等待缓存过期

        String robin = read(key);

        if (!robin.equals(value) || !testCache.get(key).equals(value)) {
            throw new RuntimeException("数据不正常...");
        }
    }


    /*
    * 更新：
		1.先写入SoR
        2.写入成功后，将缓存数据删除或者过期，下次读取时再加载缓存
    * */
    @Test
    public void updateDemo() {
        //0.先初始化SoR和Cache
        String key = "robinKey";
        String value = "robinValue";
        testService.put(key, value);
        testCache.put(key, value);


        String valueNew = "robinValueNew";
        update(key,valueNew);


        String readValueNew = read(key);
        log.info("旧数据:{},新数据:{},查询的数据:{}",value,valueNew,readValueNew);
        if(!valueNew.equals(readValueNew)){
            throw new RuntimeException("数据不一致...");
        }


    }

    /*
    *   1.先读缓存
		2.没有命中，则回源查询
		3.写入缓存
		4.返回响应
    * */
    private String read(String key) {
        //1.先读缓存
        String value = testCache.get(key);
        //2.没有命中，则回源查询
        if (StringUtils.isEmpty(value)) {
            log.info("无法从缓存中查询到Key:{},回源至SoR查询", key);
            value = testService.get(key);

            //3.写入缓存
            log.info("写入缓存Key:{},value:{}", key, value);
            testCache.put(key, value);
        } else {
            log.info("从缓存中查询出数据:key:{},value:{}", key, value);
        }
        //4.返回响应
        return value;
    }

    private void update(String key, String value) {
        //1.SoR更新数据
        testService.put(key, value);

        //2.写入成功后，将缓存数据删除或者过期，下次读取时再加载缓存
        testCache.remove(key);
    }

}
