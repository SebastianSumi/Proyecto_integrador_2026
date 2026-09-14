package pe.edu.upeu.saludablemente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Modulithic
@EnableAsync
@EnableScheduling
public class SaludablementeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaludablementeApplication.class, args);
	}

}
