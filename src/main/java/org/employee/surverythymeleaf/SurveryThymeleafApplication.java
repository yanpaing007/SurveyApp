package org.employee.surverythymeleaf;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SurveryThymeleafApplication {

    public static void main(String[] args) {

        var dotenv = Dotenv.configure()
                        .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey() ,e.getValue()));
        SpringApplication.run(SurveryThymeleafApplication.class, args);
    }

}
