package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ZkcmsConfigServerApplication {
//config server test
	public static void main(String[] args) {
		SpringApplication.run(ZkcmsConfigServerApplication.class, args);
	}

}
