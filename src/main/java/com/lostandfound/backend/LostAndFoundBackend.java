package com.lostandfound.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class LostAndFoundBackend {

	public static void main(String[] args) {
		SpringApplication.run(LostAndFoundBackend.class, args);
	}



}
