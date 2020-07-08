package jake.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private Server st;
    private Password pass;

    @BeforeEach
    public void constructServer() {
        st = new Server();
        st.readProps();
        st.setupDBConnections();

        pass = new Password();
    }

    @Test
    public void createUserTest() throws NoSuchAlgorithmException {
        // Note: An identical method of hashing is done in PasswordTest. This presumes that the password tests pass.
        String password = "looneytunesr0cks";
        byte[] passByte = pass.hash(password);
        String hashPass = pass.toHexString(passByte);

        byte[] saltByte = pass.generateSalt();
        String salt = pass.toHexString(saltByte);
        String saltedPass = hashPass + salt;

        byte[] saltedPassByte = pass.hash(saltedPass);
        String finalPass = pass.toHexString(saltedPassByte);

        st.createNewUser("bugsbunny22", finalPass, 1, 1, 1, 1);
    }

    @Test
    public void validatePermsTest() {
        Boolean actual = st.validatePerms("bugsbunny22", "editUsers");
        assertEquals(true, actual);
    }

    @Test
    public void validatePermsTestIncorrectAction() {
        assertFalse(st.validatePerms("bugsbunny22", "blhabombebouew8ahd"));
    }

    @Test
    public void validatePermsTestIncorrectUser() {
        assertFalse(st.validatePerms("dh2701ydy210sauiufhgligt", "editUsers"));
    }

    @Test
    public void addBillboardTest() {
        String actual = st.addBillboard("ltunes", "", "", "", "looney tunes is cool", "", "https://i.ytimg.com/vi/V8Hs07IQ5xk/hqdefault.jpg", "bugs bunny here", "bugsbunny22");
        assertEquals("OK", actual);
    }

    @Test
    public void addToScheduleTest() throws ClassNotFoundException {
        LocalDateTime ldt = LocalDateTime.now();
        st.addToSchedule("ltunes", ldt, Duration.ofMinutes(60),"jake");

        assertEquals("OK", st.getScheduleStartTime("ltunes"));
    }

    @Test
    public void deleteBillboardAndScheduleTest() {
        st.deleteBillboardAndSchedule("ltunes");
    }

    // Note: Even though this test "passes" it is testing against "ERROR" which would be analogous to being incorrect.
    @Test
    public void deleteUserTestDeleteSelf() {
        String actual = st.deleteUser("bugsbunny22", "bugsbunny22");
        assertEquals("ERROR", actual);
    }

    @Test
    public void deleteUserTest() {
        // jake is the strongest user of them all *queue epic music*
        String actual = st.deleteUser("jake", "bugsbunny22");
        assertEquals("OK", actual);
    }

}
