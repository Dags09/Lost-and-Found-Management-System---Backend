package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Turns on automatic timestamps: createdAt / updatedAt are filled in whenever an
// entity is saved, so you never set them by hand.
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}