package com.robin.ehcache.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Name: TestController
 * Author:  robin
 * Date: 2020/6/26 21:42
 * Description:
 **/
@RestController
public class TestController {
    @RequestMapping("/test")
    public String test(){
        System.out.println("1111111111111");
        return "123";
    }
}
