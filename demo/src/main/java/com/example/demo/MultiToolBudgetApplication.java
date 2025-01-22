package com.example.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling

public class MultiToolBudgetApplication {
	// TODO: make it possible on both phone and computer using RController
	// TODO: ADD EMAIL CONFIRMATION VIA EMAIL SMS
	// TODO: COMPRESSION HAS TO BE APPLIED CLIENT SIDE
	// TODO: SPECIFY COLUMNS OF DB TABLES
	public static void main(String[] args) {
		SpringApplication.run(MultiToolBudgetApplication.class, args);

	}
}
