package com.grace.granos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.grace.granos","com.grace.granos.model","com.grace.granos.controller","ccom.grace.granos.service","com.grace.granos.dao","com.grace.granos.util"})
@EntityScan({"com.grace.granos","com.grace.granos.model","com.grace.granos.controller","com.grace.granos.service","com.grace.granos.dao","com.grace.granos.util"})
public class GranosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GranosApplication.class, args);
	}
}
