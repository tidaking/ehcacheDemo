package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class DemoApplicationTests {

    @Autowired
    private TestServiceImpl testService;

    @Test
    void contextLoads() throws Exception {
        System.out.println(testService.test());
    }

    @Autowired
    @Qualifier("testCache")
    private Cache<String,String> testCache;

    @Autowired
    @Qualifier("testCacheManager")
    private CacheManager testCacheManager;

    @Test
    void ehCacheConfigTest(){
        String key = "robinKey";
        String value = "robinValue";
        testCache.put(key,value);
        log.info("---------------->key:{},value:{}",key,testCache.get(key));
        testCacheManager.close();
    }

    @Test
    void ehCacheInitTest(){
        //创建CacheConfigurationBuilder
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
//                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100,MemoryUnit.MB)
                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100,MemoryUnit.MB)
                //ResourcePoolsBuilder.heap(100)//heap:JVM堆 100（完全二叉树）JVM byted-size 堆缓存，速度最快；表示只能存放put100个对象，当put第101个那么前面100个对象将有一个
        );

        //cacheConfigurationBuilder--->创建CacheConfiguration
        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.build();

        //创建cacheManagerBuilder
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();

        //cacheManagerBuilder使用cacheConfiguration---->创建cacheManager
        CacheManager cacheManager =
                cacheManagerBuilder
                .withCache("testCache",cacheConfiguration)
                .build(true);//创建完之后需要初始化才能使用,true:初始化

        //cacheManager创建cache
        //一个cache,对应一个cacheManager+cacheConfiguration 一个,通过alias关联
        //
        Cache<String, String> cache
                = cacheManager.getCache("testCache", String.class, String.class);

        //往Cache里面存放数据
        String key = "robinKey";
        String value =  "robinValue";
        cache.put(key, value);

        log.info("------key:{},value:{}",key,cache.get(key));
        cacheManager.close();//通过cacheManager的close方法关闭
    }

}
