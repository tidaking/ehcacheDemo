package com.robin.ehcache.demo;

import com.robin.ehcache.demo.bean.TestBean;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.copy.Copier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Name: Test7_CopyPattern_CorrectExample
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description: 用于演示使用copyPattern
 **/
@SpringBootTest
@Slf4j
public class Test7_CopyPattern_CorrectExample {
    /*
     * 基础使用：
     * 创建自己的copier
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
    void Test1_CopyPattern_Correct_Example() {
        String cacheName = "testCache";
        //创建自己的copier
        Copier<TestBean> valueCopier = getValueCopier();

        //1.创建:CacheConfigurationBuilder
        CacheConfigurationBuilder<String, TestBean> cacheConfigurationBuilder = getCacheConfigurationBuilder();

        //2.CacheConfigurationBuilder创建:CacheConfiguration
        CacheConfiguration<String, TestBean> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder, valueCopier);

        //3.创建cacheManagerBuilder
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();

        //4.cacheManagerBuilder使用cacheConfiguration,创建CacheManager
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, cacheName);

        //5.初始化cacheManager
        cacheManager.init();

        //6.使用CacheManager获取Cache
        //一个cache,对应一个cacheManager+cacheConfiguration 一个,通过cacheName(alias)关联
        Cache<String, TestBean> cache = getCache(cacheManager, cacheName);

        //7.往Cache里面存取数据
        String key = "robinKey";
        TestBean testBean = new TestBean("1", "robin");
        cache.put(key, testBean);
        log.info(cache.get(key).toString());

        //没有操作cache，直接修改对象
        testBean.setId("2");
        testBean.setName("robin2");

        //再次读取后，cache内容并不会被污染
        log.info(cache.get(key).toString());

        //8.cacheManager的close方法关闭
        cacheManager.close();
    }


    private Copier<TestBean> getValueCopier() {
        Copier<TestBean> copier = new Copier<TestBean>() {
            @Override
            public TestBean copyForRead(TestBean obj) {
                TestBean testBean = new TestBean(obj.getId(), obj.getName());
                return testBean;
            }

            @Override
            public TestBean copyForWrite(TestBean obj) {
                TestBean testBean = new TestBean(obj.getId(), obj.getName());
                return testBean;
            }
        };
        return copier;
    }

    private CacheConfigurationBuilder<String, TestBean> getCacheConfigurationBuilder() {
        CacheConfigurationBuilder<String, TestBean> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,//Key Type
                TestBean.class,//Value Type
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(100, MemoryUnit.MB)//堆内,100MB
//                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(100, MemoryUnit.MB)//堆外,100MB
                //ResourcePoolsBuilder.heap(100)//heap:JVM堆 100（完全二叉树）JVM byted-size 堆缓存，速度最快；表示只能存放put100个对象，当put第101个那么前面100个对象将有一个
        );
        return cacheConfigurationBuilder;
    }


    private CacheConfiguration<String, TestBean> getCacheConfiguration(CacheConfigurationBuilder<String, TestBean> cacheConfigurationBuilder, Copier<TestBean> valueCopier) {
        CacheConfiguration<String, TestBean> cacheConfiguration = cacheConfigurationBuilder.
                withValueCopier(valueCopier).
                build();
        return cacheConfiguration;
    }

    private CacheManagerBuilder<CacheManager> getCacheManagerBuilder() {
        CacheManagerBuilder<CacheManager> cacheManagerCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        return cacheManagerCacheManagerBuilder;
    }

    private CacheManager getCacheManager(CacheManagerBuilder<CacheManager> cacheManagerBuilder, CacheConfiguration<String, TestBean> cacheConfiguration, String cacheName) {
        CacheManager cacheManager =
                cacheManagerBuilder
                        .withCache(cacheName, cacheConfiguration)
                        .build(false);//创建完之后需要初始化才能使用,true:初始化
        return cacheManager;
    }

    private Cache getCache(CacheManager cacheManager, String cacheName) {
        Cache<String, TestBean> cache = cacheManager.getCache(cacheName, String.class, TestBean.class);
        return cache;
    }

}
