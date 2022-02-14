package jobis.restapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ScrapNotFoundException extends RuntimeException {
    public ScrapNotFoundException(String message) {
        super(message);
    }
}