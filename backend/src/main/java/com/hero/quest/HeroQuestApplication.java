package com.hero.quest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hero.quest.mapper")
@EnableScheduling
public class HeroQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeroQuestApplication.class, args);
    }
}
