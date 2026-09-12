package com.acme.salary;

import com.acme.salary.config.SeedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SeedProperties.class)
public class SalaryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalaryManagementApplication.class, args);
	}

}
