package com.mediscreen.gateway;

import org.springframework.boot.SpringApplication;

public class TestGatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(GatewayServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
