package com.robin.ehcache.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Name: Test9_Multi_Cache
 * Author:  robin
 * Date: 2020/7/3 15:22
 * Description: 多级缓存测试
 **/
@SpringBootTest
@Slf4j
public class Test9_Multi_Cache {
    @Test
    public void readTest() {
        String key = "robinKey";
        String value = read(key);
    }


    public String read(String key){
        //1.尝试从本地缓存读取
        String value = readFromEhCache(key);

        //2 本地命中，直接返回
        if(value != null){
            return value;
        }else {
            //3 本地缓存未命中，尝试从Redis读取
            value = readFromRedis(key);

            //4 Redis命中，写入本地缓存，返回
            if(value != null){
                writeEhCache(key,value);
                return value;
            }else {
                //5.Redis未命中，回源DB查询
                value = readFromDB(key);

                //6.DB命中，则更新本地缓存，更新Redis
                if(value != null){
                    writeEhCache(key,value);
                    writeRedis(key,value);
                }
                return value;
            }
        }
    }


    public void notifyUpdateCache(){
        log.info("通知其他实例更新缓存...");
    }



    public void write(String key,String value){
        writeDB(key,value);
        deleteRedis(key);//先删除靠近SoR的
        deleteEhCache(key);
        notifyUpdateCache();//需要通知其他实例更新自己的本地缓存，可以使用Redis的订阅/发布，或者其他消息中间件通知

        //异步延时双删
        Thread thread = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.currentThread().sleep(500);
                deleteRedis(key);//先删除靠近SoR的
                deleteEhCache(key);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }




    public String readFromDB(String key){
        return null;
    }

    public void writeDB(String key,String value){


    }



    public String readFromEhCache(String key){
        return null;
    }

    public void writeEhCache(String key,String value){


    }

    public void deleteEhCache(String key){}

    public String readFromRedis(String key){
        return null;
    }


    public void writeRedis(String key,String value){
    }

    public void deleteRedis(String key){}

}
