package jobis.restapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PersonalInfoNotFoundException extends RuntimeException {
    public PersonalInfoNotFoundException(String message) {
        super(message);
    }
}
