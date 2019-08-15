package portal.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("io.openslice.model")
@SpringBootApplication()
public class PortalApplication{

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}

}