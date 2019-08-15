package portal.api.service;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "not found")
public class ItemNotFoundException extends RuntimeException {
	public ItemNotFoundException(String message) {
        super(message);
    }
	
}