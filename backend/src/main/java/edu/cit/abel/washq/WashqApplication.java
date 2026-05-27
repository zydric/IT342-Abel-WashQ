package edu.cit.abel.washq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WashqApplication {

	public static void main(String[] args) {
		SpringApplication.run(WashqApplication.class, args);
	}

}
