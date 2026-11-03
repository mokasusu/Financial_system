package com.financial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinancialBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialBeApplication.class, args);
	}

}
