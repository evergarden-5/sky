package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;


@Slf4j
public class MyTask {
    @Scheduled(cron="0/5 * * * * ?")
    public void executeTask(){
        log.info("qwq");
    }
}

