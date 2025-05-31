package br.ifsp.vvts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"br.ifsp"})
@EntityScan(basePackages = "br.ifsp.domain.models")
@EnableJpaRepositories(basePackages = "br.ifsp.application")
public class AirBnbesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirBnbesApplication.class, args);
    }

}
