package portal.api.service;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "not found")
public class PortalUserNotFoundException extends RuntimeException {
	public PortalUserNotFoundException(String message) {
        super(message);
    }
	
}