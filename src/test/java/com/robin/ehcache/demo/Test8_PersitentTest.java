package com.robin.ehcache.demo;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * Name: Test8_PersitentTest
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description: 磁盘缓存测试
 **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Slf4j
public class Test8_PersitentTest {
    private static final String cacheName = "testName";
    private static final String filePath = "C:\\7_temp\\ehcache2";
    private static final String documentName = "myData";

    /*
     * 基础测试,自定义自己的超时策略
     * */
    @Test
    void Test1_baseTest() {
        //1.创建自己的过期策略
//        ExpiryPolicy expiryPolicy = getExpiryPolicy();

        //2.创建cacheManager
//        CacheManager cacheManager = getCacheManager();
        PersistentCacheManager cacheManager = getPersistentCacheManager();


        //3.初始化cacheManager
//        cacheManager.init();

        //4.获取cache
        Cache<String, String> cache = getCache(cacheManager, cacheName);

        //5.往Cache里面存取数据
        String key = "robinKey";
        String value = "robinValue";
        cache.put(key, value);

        log.info("------key:{},value:{}", key, cache.get(key));

        //6.关闭cacheManager
        cacheManager.close();
    }


    public PersistentCacheManager getPersistentCacheManager(){
        PersistentCacheManager testPersistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File(this.filePath, this.documentName)))
                .withCache(this.cacheName, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().disk(10, MemoryUnit.MB, true))
                )
                .build(true);
        return testPersistentCacheManager;
    }


    private CacheManager getCacheManager() {
        //1.创建:CacheConfigurationBuilder
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();

        //2.CacheConfigurationBuilder创建:CacheConfiguration
        CacheConfiguration<String, String> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder);

        //3.创建cacheManagerBuilder
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();

        //4.cacheManagerBuilder使用cacheConfiguration,创建CacheManager
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, this.cacheName);


        return cacheManager;
    }


    private CacheConfigurationBuilder<String, String> getCacheConfigurationBuilder() {
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100, MemoryUnit.MB)//堆内,100MB
//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)//堆外,100MB
                //ResourcePoolsBuilder.heap(100)//heap:JVM堆 100（完全二叉树）JVM byted-size 堆缓存，速度最快；表示只能存放put100个对象，当put第101个那么前面100个对象将有一个
        );
        return cacheConfigurationBuilder;
    }

    private CacheConfiguration<String, String> getCacheConfiguration(CacheConfigurationBuilder<String, String> cacheConfigurationBuilder) {
        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.
//                withExpiry(expiryPolicy).//设置超时策略
        build();
        return cacheConfiguration;
    }


    private CacheManagerBuilder<CacheManager> getCacheManagerBuilder() {
        CacheManagerBuilder<CacheManager> cacheManagerCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        return cacheManagerCacheManagerBuilder;
    }

    private CacheManager getCacheManager(CacheManagerBuilder<CacheManager> cacheManagerBuilder, CacheConfiguration<String, String> cacheConfiguration, String cacheName) {
        CacheManager cacheManager =
                cacheManagerBuilder
                        .withCache(cacheName, cacheConfiguration)
                        .build(false);//创建完之后需要初始化才能使用,true:初始化
        return cacheManager;
    }

    private Cache getCache(PersistentCacheManager cacheManager, String cacheName) {
        Cache<String, String> cache = cacheManager.getCache(cacheName, String.class, String.class);
        return cache;
    }

}
