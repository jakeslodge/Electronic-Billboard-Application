package ControlPanel;

import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {

    private Password pass;

    @BeforeEach
    public void passwordConstruct() {
        pass = new Password();
    }

    /*
     * Unit test to ensure the plaintext password is different to the
     * hashed password.
     */
    @Test
    public void hashingWorks() throws NoSuchAlgorithmException {
        String password = "password123";
        byte[] passByte = pass.hash(password);
        String hash = pass.toHexString(passByte);
        assertNotEquals(password, hash);
    }

    @Test
    public void randomnessOfSalt() throws NoSuchAlgorithmException {
        byte[] saltByte1 = pass.generateSalt();
        String salt1 = pass.toHexString(saltByte1);
        byte[] saltByte2 = pass.generateSalt();
        String salt2 = pass.toHexString(saltByte2);
        assertNotEquals(salt1, salt2);
    }

    /*
     * Unit test to ensure when the password is salted, hashed, and then sent to the server
     * that when the password is retrieved from the database, and the same process happens, the
     * results match.
     */
    @Test
    public void checkPasswordHashes() throws NoSuchAlgorithmException {
        String password = "password123";
        byte[] passByte = pass.hash(password);
        String hashPass = pass.toHexString(passByte);

        byte[] saltByte = pass.generateSalt();
        String salt = pass.toHexString(saltByte);
        String saltedPass = hashPass + salt;

        byte[] saltedPassByte = pass.hash(saltedPass);
        String finalPass = pass.toHexString(saltedPassByte);

        byte[] comparePassByte = pass.hash(saltedPass);
        String comparePass = pass.toHexString(comparePassByte);
        assertEquals(finalPass, comparePass, "Passwords Don't Match!");
    }
}