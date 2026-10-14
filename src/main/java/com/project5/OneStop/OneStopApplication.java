package com.project5.OneStop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OneStopApplication {

	public static void main(String[] args) {
		SpringApplication.run(OneStopApplication.class, args);
	}

}
