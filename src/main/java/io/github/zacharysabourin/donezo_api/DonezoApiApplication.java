package io.github.zacharysabourin.donezo_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DonezoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonezoApiApplication.class, args);
	}

}
