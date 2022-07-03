package com.example.TcpServer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
public class TcpServerApplication {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(TcpServerApplication.class, args);
	}

}
