package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


// MongoConfig turns on automatic timestamps. With it, createdAt and updatedAt are
// filled in for you whenever you save a document, so you never set them by hand.

@Configuration
@EnableMongoAuditing

public class MongoConfig {
}
