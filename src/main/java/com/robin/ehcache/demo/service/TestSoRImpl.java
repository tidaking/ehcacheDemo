package com.robin.ehcache.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Name: TestServiceImpl
 * Author:  robin
 * Date: 2020/6/26 21:45
 * Description:
 **/
@Service
@Slf4j
public class TestSoRImpl {
    private Map<String,String> map = new HashMap();

    public String test() {
        return "123456";
    }

    public String get(String key) {
        String value = map.get(key);
        log.info("从SoR查询数据:key:{},value:{}",key,value);
        return value;
    }


    public void put(String key,String value){
        log.info("往SoR插入/更新数据:key:{},value:{}，TID:{}",key,value,Thread.currentThread().getId());
        map.put(key,value);
    }

    public void delete(String key){
        log.info("往SoR删除数据:key:{}",key);
        map.remove(key);
    }

}
