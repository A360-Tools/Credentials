/**
 * @author Sumit Kumar
 */

import com.automationanywhere.botcommand.actions.GenerateRandomPassword;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.core.security.SecureString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GenerateRandomPasswordTest {

    private GenerateRandomPassword generator;

    @BeforeClass
    public void setUp() {
        generator = new GenerateRandomPassword();
    }

    @Test
    public void testValidInputWithSpecialCharacters() {
        Value<SecureString> credentialObject = generator.action(15.0, 2.0, 3.0, 4.0, "include", "!@#$%^&*()_+", 6.0);
        assertNotNull(credentialObject);
        String password = credentialObject.get().getInsecureString();
        assertNotNull(password);
        assertEquals(15, password.length());
        assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+]).*$"));
    }

    @Test
    public void testValidInputWithoutSpecialCharacters() {
        Value<SecureString> credentialObject = generator.action(15.0, 2.0, 3.0, 4.0, "exclude", "!@#$%^&*()_+", 6.0);
        assertNotNull(credentialObject);
        String password = credentialObject.get().getInsecureString();
        assertNotNull(password);
        assertEquals(15, password.length());
        assertTrue(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]*$"));
    }

    @Test

    public void testMinimumLength() {
        Value<SecureString> credentialObject = generator.action(1.0, 1.0, 0.0, 0.0, "include", "!@#$%^&*()_+", 0.0);
        assertNotNull(credentialObject);
        String password = credentialObject.get().getInsecureString();
        assertNotNull(password);
        assertEquals(1, password.length());
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testAllParametersZero() {
        generator.action(0.0, 0.0, 0.0, 0.0, "include", "!@#$%^&*()_+", 0.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testInvalidLength() {
        generator.action(-1.0, 4.0, 4.0, 2.0, "include", "!@#$%^&*()_+", 2.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testInvalidNumLower() {
        generator.action(15.0, -1.0, 3.0, 4.0, "include", "!@#$%^&*()_+", 6.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testInvalidNumUpper() {
        generator.action(15.0, 2.0, -3.0, 4.0, "include", "!@#$%^&*()_+", 6.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testInvalidNumDigits() {
        generator.action(15.0, 2.0, 3.0, -4.0, "include", "!@#$%^&*()_+", 6.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testInvalidNumSpecial() {
        generator.action(15.0, 2.0, 3.0, 4.0, "include", "!@#$%^&*()_+", -6.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testLengthTooShort() {
        generator.action(8.0, 4.0, 4.0, 4.0, "include", "!@#$%^&*()_+", 2.0);
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testEmptyAllowedSpecialCharacters() {
        generator.action(12.0, 4.0, 4.0, 2.0, "include", "", 2.0);
    }

    @Test
    public void testSpecialCharactersExcludedAndNonEmptyAllowedSpecialCharacters() {
        Value<SecureString> credentialObject = generator.action(12.0, 4.0, 4.0, 2.0, "exclude", "!@#$%^&*()_+", 0.0);
        assertNotNull(credentialObject);
        String password = credentialObject.get().getInsecureString();
        assertNotNull(password);
        assertFalse(password.matches(".*[!@#$%^&*()_+].*"));
    }
}
