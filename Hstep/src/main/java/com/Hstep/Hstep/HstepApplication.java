package com.Hstep.Hstep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HstepApplication {

	public static void main(String[] args) {
		SpringApplication.run(HstepApplication.class, args);
	}

}
