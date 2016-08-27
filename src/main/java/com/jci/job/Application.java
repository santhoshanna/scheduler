/**
 * 
 */
package com.jci.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

import com.github.jmnarloch.spring.cloud.feign.EnableFeignAcceptGzipEncoding;

@SpringBootApplication
//@EnableCircuitBreaker
//@EnableHystrixDashboard
@EnableFeignAcceptGzipEncoding
@EnableFeignClients
//@EnableEurekaClient
public class Application {
	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);

	}
}
