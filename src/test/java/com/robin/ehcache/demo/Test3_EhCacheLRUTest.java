package com.robin.ehcache.demo;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

/**
 * Name: Test3_EhCacheLRUTest
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description:
 * <p>
 * LRU测试
 * Least Recently used
 * 最近最少使用，使用时间距离现在最久的被移除
 **/
@SpringBootTest
@Slf4j
public class Test3_EhCacheLRUTest {
    private final int maxCacheCount = 10;

    @Test
    void Test1_LRUTest() {
        String cacheName = "testCache";
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();
        CacheConfiguration<String, String> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder);
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, cacheName);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);


        //7.往Cache里面存取数据,一共存放maxCacheCount + 1条数据
        for (int i = 1; i <= maxCacheCount + 1; i++) {
            String key = i + "";
            String value = i + "";
            cache.put(key, value);
        }

        //根据LRU定义，第一个元素应该被移除
        Iterator<Cache.Entry<String, String>> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            log.info("key:{},value:{}", key, value);
            if (key.equals(1 + "")) {
                throw new RuntimeException("元素1应该被LRU移除");
            }
        }

        //8.cacheManager的close方法关闭
        cacheManager.close();
    }


    private CacheConfigurationBuilder<String, String> getCacheConfigurationBuilder() {
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
//                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100, MemoryUnit.MB)//堆内,100MB
//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)//堆外,100MB
                ResourcePoolsBuilder.heap(maxCacheCount)//heap:JVM堆 100（完全二叉树）JVM byted-size 堆缓存，速度最快；表示只能存放put100个对象，当put第101个那么前面100个对象将有一个
        );
        return cacheConfigurationBuilder;
    }

    private CacheConfiguration<String, String> getCacheConfiguration(CacheConfigurationBuilder<String, String> cacheConfigurationBuilder) {
        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.build();
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

    private Cache getCache(CacheManager cacheManager, String cacheName) {
        Cache<String, String> cache = cacheManager.getCache(cacheName, String.class, String.class);
        return cache;
    }

}
