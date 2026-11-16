package com.example.dvdrental.common;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class TimeConfiguration {
    @Bean
    Clock businessClock(@Value("${rental.time-zone:Europe/London}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
