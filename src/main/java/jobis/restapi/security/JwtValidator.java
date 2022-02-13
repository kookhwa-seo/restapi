package jobis.restapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jobis.restapi.exception.InvalidTokenException;
import jobis.restapi.exception.TokenExpiredException;
import jobis.restapi.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class JwtValidator
{
    @Value("${jwt.key}")
    private String jwtKey;

    public void validate(HttpServletRequest request) throws InvalidTokenException, TokenExpiredException {
        String token = request.getHeader("Authorization");

        if (token == null){
            throw new InvalidTokenException(String.format("token{%s} is invalid", token));
        }

        try {
            CryptoUtil.parseJWT(token, jwtKey);
        }
        catch (MalformedJwtException me) {
            throw new InvalidTokenException(String.format("token{%s} is invalid", token));
        }
        catch (ExpiredJwtException eje) {
            throw new TokenExpiredException(String.format("token{%s} is expired", token));
        }
    }
}