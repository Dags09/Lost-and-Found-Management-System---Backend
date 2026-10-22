package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on @Scheduled jobs (item expiry, token cleanup). */
@Configuration
@EnableScheduling
public class SchedulingConfig {

}