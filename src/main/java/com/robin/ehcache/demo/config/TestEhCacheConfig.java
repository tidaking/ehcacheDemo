package com.robin.ehcache.demo.config;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Expirations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Name: TestEhCacheConfig
 * Author:  robin
 * Date: 2020/6/26 23:10
 * Description: 堆内+堆外配置
 **/
@Configuration
public class TestEhCacheConfig {

    @Bean("testCacheConfigurationBuilder")
    public CacheConfiguration<String, String> testCacheConfigurationBuilder(){
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100,MemoryUnit.MB)
//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)
        );

        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.
//                withExpiry(Expirations.).
                build();
        return cacheConfiguration;
    }

    @Bean("testCacheManager")
    public CacheManager testCacheManager(@Qualifier("testCacheConfigurationBuilder") CacheConfiguration cacheConfiguration){
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        CacheManager cacheManager =
                cacheManagerBuilder
                        .withCache("testCache",cacheConfiguration)
                        .build(true);//创建完之后需要初始化才能使用,true:初始化
        return cacheManager;
    }

    @Bean("testCache")
    public Cache<String,String> testCache(@Qualifier("testCacheManager") CacheManager cacheManager ){
        Cache<String, String> cache
                = cacheManager.getCache("testCache", String.class, String.class);
        return cache;
    }

}
