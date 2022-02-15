package jobis.restapi.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha512Cipher {
    public static String encrypt(String data) {
        String afterData = "";

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-512");
            sha.update(data.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte b : sha.digest()) {
                sb.append(Integer.toHexString(0xff & b));
            }
            afterData = sb.toString();

            return afterData;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}