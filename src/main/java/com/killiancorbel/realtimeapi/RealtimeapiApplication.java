package com.killiancorbel.realtimeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealtimeapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealtimeapiApplication.class, args);
	}

}
