package jobis.restapi.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Component
public class CryptoUtil{
	private final static String enc = "UTF-8";
	private static String jwtKey;
	public static long jwtValidate;
	private static String securityKey;

	public CryptoUtil(@Value("${config.securityKey}") String securityKey, @Value("${jwt.key}") String jwtKey, @Value("${jwt.validate}") long jwtValidate){
		this.securityKey = securityKey;
		this.jwtKey = jwtKey;
		this.jwtValidate = jwtValidate;
	}

	public static SecretKeySpec getKeySpec()
	{
		byte[] bytes = new byte[32];
		SecretKeySpec spec = null;

		String keyStr = securityKey;
		bytes = keyStr.getBytes();

		//AES-256 암호화
		spec = new SecretKeySpec(bytes, "AES");
		return spec;
	}

	public static String encrypt(String text) throws Exception
	{
		if (StringUtils.isEmpty(text))
		{
			return "";
		}

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, getKeySpec());

		return Base64.encodeBytes(cipher.doFinal(text.getBytes(enc)),Base64.DONT_BREAK_LINES);
	}

	public static String decrypt(String paramText) throws Exception
	{
		if (StringUtils.isEmpty(paramText))
		{
			return "";
		}

		// base64 encoded check pattern
		String checkPattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
		if (!paramText.replace("\n", "").matches(checkPattern))  // plaintext
		{
			return paramText;
		}

		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, getKeySpec());

			byte[] bytesParamText = Base64.decode(paramText);
			if (bytesParamText == null)
			{
				return paramText;
			}

			return new String(cipher.doFinal(bytesParamText), enc);
		}
		catch (IllegalBlockSizeException e)
		{
			return paramText;
		}
	}
	public static String createJWT(String userId, String password)
	{
		HashMap<String, String> userMap = new HashMap<String, String>();

		userMap.put("userId", userId);
		userMap.put("password", password);
		String subject = JsonUtil.toJson(userMap);

		//The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		//We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtKey);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(userId)
						.setIssuedAt(now)
						.setSubject(subject)
						.signWith(signatureAlgorithm, signingKey);

		//if it has been specified, let's add the expiration
		if (jwtValidate >= 0) {
			long expMillis = nowMillis + jwtValidate;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		//Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	public static void parseJWT(String jwt, String key)
	{
		String subject = null;
		//This line will throw an exception if it is not a signed JWS (as expected)
		try
		{
			Claims claims = Jwts.parser()
							.setSigningKey(DatatypeConverter.parseBase64Binary(key))
							.parseClaimsJws(jwt).getBody();

			subject = claims.getSubject();

		}
		catch (ExpiredJwtException eje)
		{
			throw eje;
		}
		catch (MalformedJwtException me)
		{
			throw me;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}