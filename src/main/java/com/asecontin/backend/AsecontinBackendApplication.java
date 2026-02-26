package com.asecontin.backend;

import com.asecontin.backend.config.JwtProperties;
import com.asecontin.backend.config.MediaProperties;
import com.asecontin.backend.config.WhatsappProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, MediaProperties.class, WhatsappProperties.class })
@EnableScheduling
public class AsecontinBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsecontinBackendApplication.class, args);
	}

}
