//package com.robin.ehcache.demo.config;
//
//import org.ehcache.Cache;
//import org.ehcache.CacheManager;
//import org.ehcache.PersistentCacheManager;
//import org.ehcache.config.CacheConfiguration;
//import org.ehcache.config.builders.CacheConfigurationBuilder;
//import org.ehcache.config.builders.CacheManagerBuilder;
//import org.ehcache.config.builders.ResourcePoolsBuilder;
//import org.ehcache.config.units.MemoryUnit;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.File;
//
///**
// * Name: TestEhCacheConfig
// * Author:  robin
// * Date: 2020/6/26 23:10
// * Description: 磁盘配置
// **/
//@Configuration
//public class TestPersistentEhCacheConfig {
//
//    @Bean("testPersistentCacheManager")
//    public PersistentCacheManager testPersistentCacheManager(){
//        PersistentCacheManager testPersistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
//                .with(CacheManagerBuilder.persistence(new File("C:\\7_temp\\ehcache", "myData")))
//                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
//                        ResourcePoolsBuilder.newResourcePoolsBuilder().disk(10, MemoryUnit.MB, true))
//                )
//                .build(true);
//        return testPersistentCacheManager;
//    }
//
//
//
//    @Bean("testPersistentCache")
//    public Cache<String,String> testCache(@Qualifier("testPersistentCacheManager") PersistentCacheManager cacheManager ){
//        Cache<String, String> cache
//                = cacheManager.getCache("testCache", String.class, String.class);
//        return cache;
//    }
//
//}
