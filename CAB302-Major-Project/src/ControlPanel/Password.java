package ControlPanel;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import BillboardViewer.*;

public class Password {

    public Password() {

    }

    /**
     * Hash the password into an array of bytes
     * @param hashValue password to be hashed
     * @return resulting hashed password as byte array
     * @throws NoSuchAlgorithmException
     */
    public byte[] hash(String hashValue) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashPass = md.digest(hashValue.getBytes(StandardCharsets.UTF_8));
        return hashPass;
    }

    /**
     * Generates salt to add to the password
     * @return the salt
     */
    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltByte = new byte[16];
        random.nextBytes(saltByte);
        return saltByte;
    }

    /**
     * Converts the hashed byte array into a hex string
     * @param hash hashed password as byte array
     * @return hashed password as hex string
     */
    public String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

}
