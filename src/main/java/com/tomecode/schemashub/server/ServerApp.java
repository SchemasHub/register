package com.tomecode.schemashub.server;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 
 * @author Tomas.Frastia@erstegroupIT.com
 *
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class ServerApp {

	public static final void main(String args[]) {
		//
		System.setProperty("server.port", "9999");
		SpringApplication.run(ServerApp.class, args);
	}

	@ExceptionHandler(Exception.class)
	public final String fooException(HttpServletRequest request, Throwable e) {
		e.printStackTrace();
		return e.getMessage();
	}

}
