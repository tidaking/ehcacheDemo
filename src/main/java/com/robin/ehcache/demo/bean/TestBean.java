package com.robin.ehcache.demo.bean;

import lombok.Data;

/**
 * Name: TestBean
 * Author:  robin
 * Date: 2020/7/3 10:35
 * Description: ${DESCRIPTION}
 **/
@Data
public class TestBean {
    private String id;
    private String name;

    public TestBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
