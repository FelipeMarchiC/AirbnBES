package br.ifsp.vvts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "br.ifsp")
public class DemoAuthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoAuthAppApplication.class, args);
    }

}
