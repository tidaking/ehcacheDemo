package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestSoRImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Name: Test5_CacheAsSoR
 * Author:  robin
 * Date: 2020/7/3 10:04
 * Description:
 * 测试CacheAsSoR模式
 **/
@SpringBootTest
@Slf4j
public class Test5_CacheAsSoR {
    private Cache<String, String> cache = getTestCache();

    @Autowired
    private TestSoRImpl testSoR;

    @Test
    public void Test1_Write(){
        String key = "testKey";
        String value = "testValue";

        cache.put(key,value);
    }


    @Test
    public void Test2_Read(){
        String key = "testKey";
        String value = "testValue";

        cache.put(key,value);
        String readValue = cache.get(key);
        log.info("读取key:{},value:{}",key,readValue);
    }


    @Test
    public void Test3_Delete(){
        String key = "testKey";
        String value = "testValue";

        cache.put(key,value);
        log.info("读取key:{},value:{}",key,cache.get(key));
        String readValue = cache.get(key);
        log.info("读取key:{},value:{}",key,readValue);


        cache.remove(key);
        String readValue2 = cache.get(key);
        log.info("读取key:{},value:{}",key,readValue2);

    }



    private Cache getTestCache() {
        String cacheName = "testCache";
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();
        CacheLoaderWriter<String, String> cacheLoaderWriter = getCacheLoaderWriter();
        CacheConfiguration<String, String> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder,cacheLoaderWriter);
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, cacheName);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);
        return cache;
    }

   /*
   * NOTE:
   * 在这里，定义自己的CacheLoaderWriter
   *
   * */
    private CacheLoaderWriter<String, String> getCacheLoaderWriter() {
        CacheLoaderWriter<String, String> cacheLoaderWriter = new CacheLoaderWriter<String, String>() {
            @Override
            public String load(String key) throws Exception {
                log.info("通过writer进行load操作...");
                String value = testSoR.get(key);
                return value;
            }

            @Override
            public void write(String key, String value) throws Exception {
                log.info("通过writer进行write操作...");
                testSoR.put(key, value);
            }

            @Override
            public void delete(String key) throws Exception {
                log.info("通过writer进行delete操作...");
                testSoR.delete(key);
            }
        };

        return cacheLoaderWriter;
    }


    private CacheConfigurationBuilder<String, String> getCacheConfigurationBuilder() {
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100, MemoryUnit.MB)//堆内,100MB
//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)//堆外,100MB
//                ResourcePoolsBuilder.heap(maxCacheCount)//heap:JVM堆 100（完全二叉树）JVM byted-size 堆缓存，速度最快；表示只能存放put100个对象，当put第101个那么前面100个对象将有一个
        );
        return cacheConfigurationBuilder;
    }

    private CacheConfiguration<String, String> getCacheConfiguration(CacheConfigurationBuilder<String, String> cacheConfigurationBuilder,
                                                                     CacheLoaderWriter<String, String> cacheLoaderWriter) {
        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.
                withLoaderWriter(cacheLoaderWriter).
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

    private Cache getCache(CacheManager cacheManager, String cacheName) {
        Cache<String, String> cache = cacheManager.getCache(cacheName, String.class, String.class);
        return cache;
    }


}
