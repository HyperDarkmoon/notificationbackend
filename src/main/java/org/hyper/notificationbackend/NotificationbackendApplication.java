package org.hyper.notificationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationbackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationbackendApplication.class, args);
    }
}
