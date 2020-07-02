package com.robin.ehcache.demo;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Name: Test1_EhCacheBaseTest
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description: 基础测试，主要是介绍EhCache基础使用
 **/
@SpringBootTest
@Slf4j
public class Test1_EhCacheBaseTest {
    /*
     * 基础使用：
     * 1.创建:CacheConfigurationBuilder
     * 2.CacheConfigurationBuilder创建:CacheConfiguration
     * 3.创建cacheManagerBuilder
     * 4.cacheManagerBuilder使用cacheConfiguration,创建CacheManager
     * 5.初始化cacheManager
     * 6.使用CacheManager获取Cache
     * 7.往Cache里面存取数据
     * 8.cacheManager的close方法关闭
     * */
    @Test
    void Test1_ehCacheInitTest() {
        String cacheName = "testCache";

        //1.创建:CacheConfigurationBuilder
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();

        //2.CacheConfigurationBuilder创建:CacheConfiguration
        CacheConfiguration<String, String> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder);

        //3.创建cacheManagerBuilder
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();

        //4.cacheManagerBuilder使用cacheConfiguration,创建CacheManager
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, cacheName);

        //5.初始化cacheManager
        cacheManager.init();

        //6.使用CacheManager获取Cache
        //一个cache,对应一个cacheManager+cacheConfiguration 一个,通过cacheName(alias)关联
        Cache<String, String> cache = getCache(cacheManager, cacheName);

        //7.往Cache里面存取数据
        String key = "robinKey";
        String value = "robinValue";
        cache.put(key, value);

        log.info("------key:{},value:{}", key, cache.get(key));

        //8.cacheManager的close方法关闭
        cacheManager.close();
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
