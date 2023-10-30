package de.sixt.allane.kestler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/** The entry point for the project. Boilerplate */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class KestlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KestlerApplication.class, args);
	}
}
