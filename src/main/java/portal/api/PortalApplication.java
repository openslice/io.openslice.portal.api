package portal.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Our API docs are at:
 * http://localhost:13000/osapi/swagger-ui.html
 * http://localhost:13000/osapi/v2/api-docs
 * 
 * @author ctranoris
 *
 */
@EntityScan("io.openslice.model")
@SpringBootApplication()
public class PortalApplication{

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}

}