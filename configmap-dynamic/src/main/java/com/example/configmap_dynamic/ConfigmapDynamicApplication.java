package com.example.configmap_dynamic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConfigmapDynamicApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigmapDynamicApplication.class, args);
	}

}
