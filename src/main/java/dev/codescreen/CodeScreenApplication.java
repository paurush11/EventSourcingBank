package dev.codescreen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableAutoConfiguration
public class CodeScreenApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeScreenApplication.class, args);
    }
}
