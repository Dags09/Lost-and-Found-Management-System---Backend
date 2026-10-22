package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


/** Turns on JPA auditing, which fills the @CreatedDate / @LastModifiedDate fields automatically. */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}