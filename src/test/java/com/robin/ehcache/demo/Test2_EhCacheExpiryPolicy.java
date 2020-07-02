package com.robin.ehcache.demo;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Name: Test2_EhCacheExpiryPolicy
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description: 超时回收策略测试
 **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Slf4j
public class Test2_EhCacheExpiryPolicy {
    private static final String cacheName = "testName";
    private static long createTTL = 5L;
    private static long accessTTL = 4L;
    private static long updateTTL = 3L;

    /*
    * 基础测试,自定义自己的超时策略
    * */
    @Test
    void Test1_baseTest() {
        //1.创建自己的过期策略
        ExpiryPolicy expiryPolicy = getExpiryPolicy();

        //2.创建cacheManager
        CacheManager cacheManager = getCacheManager(expiryPolicy);

        //3.初始化cacheManager
        cacheManager.init();

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


    /*
     *
     * 测试超时:创建
     * */
    @Test
    void Test2_ExpiryForCreationTest() throws InterruptedException {
        String key = "robinKey";
        String value = "robinValue";
        ExpiryPolicy expiryPolicy = getExpiryPolicy();
        CacheManager cacheManager = getCacheManager(expiryPolicy);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);

        //5.往Cache里面存取数据
        cache.put(key, value);

        //6.睡眠:createTTL + 1s
        Thread.currentThread().sleep((createTTL + 1) * 1000);

        if (value.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该已失效");
        }

        //6.关闭cacheManager
        cacheManager.close();
    }

    /*
     *
     * 测试超时:获取
     * */
    @Test
    void Test3_ExpiryForAccessTest() throws InterruptedException {
        String key = "robinKey";
        String value = "robinValue";
        ExpiryPolicy expiryPolicy = getExpiryPolicy();
        CacheManager cacheManager = getCacheManager(expiryPolicy);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);

        //5.往Cache里面存取数据
        cache.put(key, value);
        //6.马上读取
        if (!value.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该未失效");
        }

        //7.在上次读取完之后,在超时前,再读取一次
        Thread.currentThread().sleep((accessTTL - 1) * 1000);
        if (!value.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该未失效");
        }

        //8.在上次读取完之后,在超时后,再读取一次
        Thread.currentThread().sleep((accessTTL + 1) * 1000);
        if (value.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该已失效");
        }

        //6.关闭cacheManager
        cacheManager.close();
    }


    /*
     *
     * 测试超时:更新
     * */
    @Test
    void Test3_ExpiryForUpdateTest() throws InterruptedException {
        String key = "robinKey";
        String value = "robinValue";
        String newValue = "robinValue_new";
        ExpiryPolicy expiryPolicy = getExpiryPolicy();
        CacheManager cacheManager = getCacheManager(expiryPolicy);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);

        //5.往Cache里面存取数据
        cache.put(key, value);

        //6.马上读取
        if (!value.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该未失效");
        }

        //7.更新
        cache.put(key, newValue);
        Thread.currentThread().sleep((updateTTL - 1) * 1000);
        if (!newValue.equals(cache.get(key))) {
            throw new RuntimeException("该缓存应该未失效");
        }

        //7.重新更新
        cache.put(key, value);
        Thread.currentThread().sleep((updateTTL + 1) * 1000);
        if (cache.get(key) != null) {
            throw new RuntimeException("该缓存应该已失效");
        }

        //6.关闭cacheManager
        cacheManager.close();
    }


    private CacheManager getCacheManager(ExpiryPolicy expiryPolicy) {
        //1.创建:CacheConfigurationBuilder
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();

        //2.CacheConfigurationBuilder创建:CacheConfiguration
        CacheConfiguration<String, String> cacheConfiguration = getExpiryPolicyCacheConfiguration(cacheConfigurationBuilder, expiryPolicy);

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

    private CacheConfiguration<String, String> getExpiryPolicyCacheConfiguration(CacheConfigurationBuilder<String, String> cacheConfigurationBuilder, ExpiryPolicy expiryPolicy) {
        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.
                withExpiry(expiryPolicy).//设置超时策略
                build();
        return cacheConfiguration;
    }

    public ExpiryPolicy getExpiryPolicy() {
        ExpiryPolicy<String, String> expiryPolicy = new ExpiryPolicy<String, String>() {
            /*
             *  缓存被创建时的有效时间
             * */
            @Override
            public Duration getExpiryForCreation(String key, String value) {
                return Duration.ofSeconds(createTTL);
            }

            /*
             *  缓存被访问时的有效时间
             * */
            @Override
            public Duration getExpiryForAccess(String key, Supplier<? extends String> value) {
                return Duration.ofSeconds(accessTTL);
            }

            /*
             *  缓存被更新时的有效时间
             * */
            @Override
            public Duration getExpiryForUpdate(String key, Supplier<? extends String> oldValue, String newValue) {
                return Duration.ofSeconds(updateTTL);
            }
        };
        return expiryPolicy;
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
