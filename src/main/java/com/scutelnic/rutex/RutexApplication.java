package com.scutelnic.rutex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RutexApplication {

	public static void main(String[] args) {
		SpringApplication.run(RutexApplication.class, args);
	}

}
