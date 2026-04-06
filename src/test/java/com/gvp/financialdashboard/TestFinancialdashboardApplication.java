package com.gvp.financialdashboard;

import org.springframework.boot.SpringApplication;

public class TestFinancialdashboardApplication {

	public static void main(String[] args) {
		SpringApplication.from(FinancialdashboardApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
