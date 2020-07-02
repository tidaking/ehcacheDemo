package com.robin.ehcache.demo;

import com.robin.ehcache.demo.service.TestSoRImpl;
import lombok.SneakyThrows;
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
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Name: Test4_CacheAsideTest
 * Author:  robin
 * Date: 2020/7/2 20:21
 * Description:
 * <p>
 * 测试CacheAside模式
 **/
@SpringBootTest
@Slf4j
public class Test4_CacheAsideTest {
    private Cache<String, String> cache = getTestCache();

    @Autowired
    private TestSoRImpl testSoR;


    /*
    测试1:读数据示范
    1.先读缓存
    2.没有命中，则回源查询
    ------------->严格来说，2+3并不是原子操作，所以有个并发更新SoR的操作的话，会导致缓存中存的是旧数据
    ------------->真的要解决这种情况，那就用 超时 / 加锁 / 延时双删方案吧
    3.写入缓存
    4.返回响应
    * */
    @Test
    public void Test1_ReadExample() {
        //初始化数据
        String key = "testKey";
        String initValue = "testValue";
        this.cache.clear();
        testSoR.put(key, initValue);

        String value = read(key);
        log.info("查询结果key:{},value:{}", key, value);
    }

    /*
     * 测试2:
     * 先更新数据库，再更新缓存
     * 这种做法最大的问题就是两个并发的写操作导致脏数据。
     * 两个并发更新操作，数据库先更新的反而后更新缓存，数据库后更新的反而先更新缓存。
     * 这样就会造成数据库和缓存中的数据不一致，应用程序中读取的都是脏数据。
     * */
    @Test
    void Test2_WrongUpdateExample() throws InterruptedException {
        //1.创建2个线程
        //线程A更新SoR,value:A
        //线程B更新SoR,value:B
        //线程B更新缓存,value:B
        //线程A更新缓存，value:A

        ///线程A
        //更新SoR------------------1000ms-------------更新缓存

        ///线程B
        //线程A启动100ms-----更新SoR----100ms--更新缓存
        String key = "testkey";
        Thread threadA = getUpdateSoR_UpdateCacheThread("T-A", cache, key, "testValue_A", 1000L);
        Thread threadB = getUpdateSoR_UpdateCacheThread("T-B", cache, key, "testValue_B", 100L);
        threadA.start();
        Thread.currentThread().sleep(100);
        threadB.start();

        threadA.join();
        threadB.join();

        log.info("获取key:{},value:{}", key, cache.get(key));
    }


    /*
     * 测试3:
     * 先删除缓存，再更新数据库。
     * 这个逻辑是错误的，因为两个并发的读和写操作导致脏数据。
     * 假设更新操作先删除了缓存，此时正好有一个并发的读操作，没有命中缓存后从数据库中取出老数据并且更新回缓存，
     * 这个时候更新操作也完成了数据库更新。此时，数据库和缓存中的数据不一致，应用程序中读取的都是原来的数据（脏数据）。
     *
     * */
    @Test
    void Test3_WrongUpdateExample() throws InterruptedException {
        //1.创建2个线程
        //线程A删除缓存value_old
        //线程B尝试从缓存获取,失败
        //线程B回源，获取到旧值,value_old
        //线程B讲旧值写入缓存，value_old
        //线程A更新SoR:value_new

        ///线程A
        //删除缓存------------------1000ms----------------------------------------更新SoR

        ///线程B
        //线程A启动100ms-----尝试读缓存+回源查询+写入缓存----100ms--更新缓存
        String key = "testkey";
        String oldValue = "oldValue";
        String newValue = "newValue";
        testSoR.put(key, oldValue);
        this.cache.put(key, oldValue);


        Thread threadA = getDeleteCache_UpdateSoRThread("T-A", cache, key, newValue, 1000L);
        Thread threadB = getReadThread("T-B", cache, key);
        threadA.start();
        Thread.currentThread().sleep(100);
        threadB.start();

        threadA.join();
        threadB.join();

        log.info("获取key:{},value:{}", key, cache.get(key));
    }



    /*
     * 测试4:
     * 先更新数据库，再删除缓存。
     * 这种做法其实不能算是坑，在实际的系统中也推荐使用这种方式。
     * 但是这种方式理论上还是可能存在问题。
     * 查询操作没有命中缓存，然后查询出数据库的老数据。
     * 此时有一个并发的更新操作，更新操作在读操作之后更新了数据库中的数据并且删除了缓存中的数据。
     * 然而读操作将从数据库中读取出的老数据更新回了缓存。这样就会造成数据库和缓存中的数据不一致，应用程序中读取的都是原来的数据（脏数据）。
     *
     * 但是，仔细想一想，这种并发的概率极低。
     * 因为这个条件需要：
     * 1.发生在读缓存时缓存失效，而且有一个并发的写操作。
     * 实际上数据库的写操作会比读操作慢得多，而且还要加锁，
     * 2.而读操作必需在写操作前进入数据库操作，
     * 3.又要晚于写操作更新缓存，
     * 所有这些条件都具备的概率并不大。
     * 但是为了避免这种极端情况造成脏数据所产生的影响，我们还是要为缓存设置过期时间。
     * 或者延时异步双删
     * */
    @Test
    void Test4_UpdateExample() throws InterruptedException {
        //1.创建2个线程
        //线程A更新SoR

        //线程A查询，未命中缓存
        //线程A回源查询出旧值,value_old
        //线程B更新SoR，value_new
        //线程B将删除缓存
        //线程A更新缓存:value_old

        ///线程A
        //查询，未命中缓存+回源----------1000ms----------------------------------------更新缓存

        ///线程B
        //线程A启动100ms----------更新SoR + 删除缓存
        String key = "testkey";
        String oldValue = "oldValue";
        String newValue = "newValue";
        testSoR.put(key, oldValue);
        this.cache.clear();


        Thread threadA = getReadThread("T-A", cache, key,1000L);
        //3000ms后执行异步延时双删，删除3000ms内缓存的脏数据
        Thread threadB = getDeleteCache_UpdateSoRThread_AsyncDoubleDelete("T-B", cache, key, newValue, 1L,
                3000L);
        threadA.start();
        Thread.currentThread().sleep(100);
        threadB.start();

        threadA.join();
        threadB.join();

        for(int i = 0;i<60;i++){
            log.info("获取key:{},value:{}", key, read(key));
            Thread.currentThread().sleep(100);
        }
    }

    private Thread getDeleteCache_UpdateSoRThread_AsyncDoubleDelete(String threadName, Cache cache, String key, String value,
                                                                    Long updateSoRTimeMs,
                                                                    Long doubleDeleteTime) {
        Runnable doubleDeleteRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                log.info("异步双删开始,key:{}.....",key);
                Thread.currentThread().sleep(doubleDeleteTime);
                cache.remove(key);
                log.info("异步双删结束,key:{}.....",key);
            }
        };


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //先删除缓存，再更新SoR
                    cache.remove(key);
                    updateSoR(key, value, updateSoRTimeMs);

                    Thread doubleDeleteThread = new Thread(doubleDeleteRunnable);
                    doubleDeleteThread.setDaemon(true);
                    doubleDeleteThread.start();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        return thread;
    }

    public String read(String key) {
        String value = this.cache.get(key);
        if (value == null) {
            log.info("缓存中无此数据,key:{}", key);
            value = testSoR.get(key);
            log.info("回源查询,key:{},value:{}", key, value);
            this.cache.put(key, value);
        }
        return value;
    }

    public String read(String key,Long timeBeforeUpdateCache) throws InterruptedException {
        String value = this.cache.get(key);
        if (value == null) {
            log.info("缓存中无此数据,key:{}", key);
            value = testSoR.get(key);
            Thread.currentThread().sleep(timeBeforeUpdateCache);
            log.info("回源查询,key:{},value:{}", key, value);
            this.cache.put(key, value);
        }
        return value;
    }


    private Thread getReadThread(String threadName, Cache<String, String> cache, String key) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("开始读取:key:{}",key);
                String value = read(key);
                log.info("读取完成:key:{},value:{}",key,value);
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        return thread;
    }

    private Thread getReadThread(String threadName, Cache<String, String> cache, String key,Long timeBeforeUpdateCache) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("开始读取:key:{}",key);
                String value = null;
                try {
                    value = read(key,timeBeforeUpdateCache);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
                log.info("读取完成:key:{},value:{}",key,value);
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        return thread;
    }

    private Thread getDeleteCache_UpdateSoRThread(String threadName, Cache cache, String key, String value, Long updateSoRTimeMs) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //先删除缓存，再更新SoR
                    cache.remove(key);
                    updateSoR(key, value, updateSoRTimeMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        return thread;
    }

    private Thread getUpdateSoR_UpdateCacheThread(String threadName, Cache cache, String key, String value, Long updateSoRTimeMs) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //先更新数据库
                    updateSoR(key, value, updateSoRTimeMs);
                    //再更新缓存
                    cache.put(key, value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        return thread;
    }


    private void updateSoR(String key, String value, Long sleepTimeMs) throws InterruptedException {
        log.info("开始更新SoR,key:{},value:{}", key, value);
        Thread.currentThread().sleep(sleepTimeMs);
        testSoR.put(key, value);
        log.info("更新SoR成功，key:{},value:{}", key, value);
    }

    private Cache getTestCache() {
        String cacheName = "testCache";
        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = getCacheConfigurationBuilder();
        CacheConfiguration<String, String> cacheConfiguration = getCacheConfiguration(cacheConfigurationBuilder);
        CacheManagerBuilder<CacheManager> cacheManagerBuilder = getCacheManagerBuilder();
        CacheManager cacheManager = getCacheManager(cacheManagerBuilder, cacheConfiguration, cacheName);
        cacheManager.init();
        Cache<String, String> cache = getCache(cacheManager, cacheName);
        return cache;
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
