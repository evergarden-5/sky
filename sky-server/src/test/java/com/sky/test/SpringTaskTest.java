package com.sky.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootTest
public class SpringTaskTest {

    @EnableScheduling("")
    public void scheduleTest(){

    }
}
