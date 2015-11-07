package demo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableBatchProcessing
@EnableAutoConfiguration
@ImportResource("spring-jobs.xml")
public class BlogsCsvDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogsCsvDemoApplication.class, args);
    }
}
