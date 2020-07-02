package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestSoRImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;


/*
    Cache-As-SoR

    Read-Through


    Write-Through



    Write-Behind

* */
@SpringBootTest
@Slf4j
class CacheAsSoRTest {

    @Autowired
    private TestSoRImpl testService;


    @Autowired
    @Qualifier("cacheAsSoR_testCache")
    private Cache<String, String> testCache;

    @Autowired
    @Qualifier("cacheAsSoR_testCacheManager")
    private CacheManager testCacheManager;



    /*
    *
    * 写
    * */
    @Test
    public void writeTest() {
        testCache.put("robinKey", "robinValue");

        log.info("查询结果:{}", testCache.get("robinKey"));
    }

    /*
     *
     * 读
     * */
    @Test
    public void readTest() {
        String key = "robinKey";
        String value = "robinValue";

        testCache.put(key, value);
        log.info("查询结果:{}", testCache.get(key));

        //往SoR中存入一个缓存中没有的值
        String newKey = "robinKey_new";
        String newValue = "robinValue_new";
        testService.put(newKey,newValue);

        log.info("第二次查询结果:{}", testCache.get(newKey));
        log.info("第三次查询结果（直接从缓存里面读取）:{}", testCache.get(newKey));


    }

    @Test
    public void deleteTest(){
        String key = "robinKey";
        String value = "robinValue";

        testCache.put(key, value);
        log.info("查询结果:{}", testCache.get(key));


        testCache.remove(key);
        log.info("查询结果:{}", testCache.get(key));
    }


    @Test
    public void writeBatchTest() throws InterruptedException {
        testCache.put("robinKey", "robinValue");
        testCache.put("robinKey"+"1", "robinValue"+"1");
        testCache.put("robinKey"+"2", "robinValue"+"2");
        testCache.put("robinKey"+"3", "robinValue"+"3");
        testCache.put("robinKey"+"4", "robinValue"+"4");
        testCache.put("robinKey"+"5", "robinValue"+"5");
        testCache.put("robinKey"+"6", "robinValue"+"6");
        testCache.put("robinKey"+"7", "robinValue"+"7");
        log.info("先睡一觉....");

        Thread.currentThread().sleep(10*1000L);
        log.info("查询结果:{}", testCache.get("robinKey"));
    }


}
