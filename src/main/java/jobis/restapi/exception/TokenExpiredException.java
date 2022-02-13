package jobis.restapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException(String msg) {
		super(msg);
	}
}
