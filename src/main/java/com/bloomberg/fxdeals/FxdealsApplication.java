package com.bloomberg.fxdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(basePackages = { "com.bloomberg" })
@EntityScan("com.bloomberg")
@EnableJpaRepositories(basePackages = "com.bloomberg.**.repo")

public class FxdealsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FxdealsApplication.class, args);
	}

}
