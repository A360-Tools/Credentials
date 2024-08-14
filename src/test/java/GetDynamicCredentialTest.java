import com.automationanywhere.botcommand.actions.GetDynamicCredential;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.core.security.SecureString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.automationanywhere.utilities.AuthenticationUtils.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class GetDynamicCredentialTest {

    private static final String TEST_CR_URL = "https://community.cloud.automationanywhere.digital/";
    private static final String TEST_USERNAME = "your_username";
    private static final String TEST_PASSWORD = "your_password";
    private static final String TEST_CREDENTIAL_NAME = "test_credential";
    private static final String TEST_ATTRIBUTE_NAME = "password";

    private GetDynamicCredential getDynamicCredential;

    @BeforeClass
    public void setUp() {
        getDynamicCredential = new GetDynamicCredential();
    }

    @Test
    public void testExecuteWithSpecificUser() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();

        // Act
        Value<SecureString> result = getDynamicCredential.execute(
                TEST_CREDENTIAL_NAME,
                TEST_ATTRIBUTE_NAME,
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.get());
        assertFalse(result.get().getInsecureString().isEmpty());
        System.out.println("Retrieved credential value: " + result.get().getInsecureString());
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testExecuteWithInvalidCredentialName() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();

        // Act
        getDynamicCredential.execute(
                "invalid_credential_name",
                TEST_ATTRIBUTE_NAME,
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2
        );

        // Assert: BotCommandException is expected
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testExecuteWithInvalidAttributeName() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();

        // Act
        getDynamicCredential.execute(
                TEST_CREDENTIAL_NAME,
                "invalid_attribute_name",
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2
        );

        // Assert: BotCommandException is expected
    }
}