package com.nithin.addressmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AddressManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AddressManagementSystemApplication.class, args);
	}
}