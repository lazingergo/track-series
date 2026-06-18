package com.trackseries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrackSeriesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackSeriesBackendApplication.class, args);
    }

}
