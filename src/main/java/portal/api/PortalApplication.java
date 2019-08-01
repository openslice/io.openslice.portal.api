package portal.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@EntityScan( "io.openslice.model" )
@SpringBootApplication() 
public class PortalApplication {


	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}
}