package cl.legga.legajos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MsLegajosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsLegajosApplication.class, args);
	}

}