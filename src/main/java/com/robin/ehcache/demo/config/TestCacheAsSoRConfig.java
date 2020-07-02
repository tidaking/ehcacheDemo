package com.robin.ehcache.demo.config;

import com.robin.ehcache.demo.service.TestSoRImpl;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.WriteBehindConfigurationBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Name: TestCacheAsSoRConfig
 * Author:  robin
 * Date: 2020/6/26 23:10
 * Description: CacheAsSoR配置
 **/
@Configuration
@Slf4j
public class TestCacheAsSoRConfig {

    @Autowired
    private TestSoRImpl testService;


    @Bean("cacheAsSoR_testCacheLoaderWriter")
    public CacheLoaderWriter<String, String> cacheLoaderWriter() {
        CacheLoaderWriter<String, String> cacheLoaderWriter = new CacheLoaderWriter<String, String>() {
            @Override
            public String load(String key) throws Exception {
                log.info("通过writer进行load操作...");
                String value = testService.get(key);
                return value;
            }

            @Override
            public void write(String key, String value) throws Exception {
                log.info("通过writer进行write操作...");
                testService.put(key, value);
            }

            @Override
            public void delete(String key) throws Exception {
                log.info("通过writer进行delete操作...");
                testService.delete(key);
            }

        };

        return cacheLoaderWriter;
    }


    @Bean("cacheAsSoR_testCacheConfigurationBuilder")
    public CacheConfiguration<String, String> testCacheConfigurationBuilder(@Qualifier("testExpiryPolicy") ExpiryPolicy expiryPolicy,
                                                                            @Qualifier("cacheAsSoR_testCacheLoaderWriter") CacheLoaderWriter<String, String> cacheLoaderWriter
    ) {
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                String.class,//Value Type
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(100, MemoryUnit.MB)

//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)
        );

        CacheConfiguration<String, String> cacheConfiguration = cacheConfigurationBuilder.
                withExpiry(expiryPolicy).
                withLoaderWriter(cacheLoaderWriter).//注册自己的writer

                withService(WriteBehindConfigurationBuilder//支持Write-Behind
                .newBatchedWriteBehindConfiguration(5, TimeUnit.SECONDS, 60)
                .queueSize(120)//Write-Behind是异步的，是先放入队列中的，所以可以定义等待队列最大大小
                .concurrencyLevel(1)//使用多少个并发队列，所以最大队列大小为concurrencyLevel * queueSize * batchSize
                .enableCoalescing()).

                build();
        return cacheConfiguration;
    }

    @Bean("cacheAsSoR_testCacheManager")
    public CacheManager testCacheManager(@Qualifier("cacheAsSoR_testCacheConfigurationBuilder") CacheConfiguration cacheConfiguration) {
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        CacheManager cacheManager =
                cacheManagerBuilder
                        .withCache("testCache", cacheConfiguration)
                        .build(true);//创建完之后需要初始化才能使用,true:初始化
        return cacheManager;
    }

    @Bean("cacheAsSoR_testCache")
    public Cache<String, String> testCache(@Qualifier("cacheAsSoR_testCacheManager") CacheManager cacheManager) {
        Cache<String, String> cache
                = cacheManager.getCache("testCache", String.class, String.class);
        return cache;
    }

}
