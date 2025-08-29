package com.mediscreen.frontend;

import org.springframework.boot.SpringApplication;

public class TestFrontendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(FrontendServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
