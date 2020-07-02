package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestSoRImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class TestPersitentEhCacheTest {

    @Autowired
    private TestSoRImpl testService;


    @Autowired
    @Qualifier("testCache")
    private Cache<String,String> testCache;

    @Autowired
    @Qualifier("testPersistentCacheManager")
    private PersistentCacheManager testPersistentCacheManager;

    @Test
    void ehCacheConfigTest(){
        String key = "robinKey";
        String value = "robinValue";
        testCache.put(key,value);
        log.info("---------------->key:{},value:{}",key,testCache.get(key));
        testPersistentCacheManager.close();
    }


}
