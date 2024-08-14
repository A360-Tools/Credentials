import com.automationanywhere.botcommand.actions.UpdateDynamicCredential;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.core.security.SecureString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.automationanywhere.utilities.AuthenticationUtils.*;

public class UpdateDynamicCredentialTest {

    private static final String TEST_CR_URL = "https://community.cloud.automationanywhere.digital/";
    private static final String TEST_USERNAME = "your_username";
    private static final String TEST_PASSWORD = "your_password";
    private static final String TEST_CREDENTIAL_NAME = "test_credential";
    private static final String TEST_ATTRIBUTE_NAME = "password";

    private UpdateDynamicCredential updateDynamicCredential;

    @BeforeClass
    public void setUp() {
        updateDynamicCredential = new UpdateDynamicCredential();
    }

    @Test
    public void testExecuteWithSpecificUser() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();
        SecureString newValue = new CredentialObject("new_test_password").get();

        // Act
        updateDynamicCredential.execute(
                TEST_CREDENTIAL_NAME,
                TEST_ATTRIBUTE_NAME,
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2,
                newValue,
                ""
        );

        // Assert: If no exception is thrown, the test is considered successful
        System.out.println("Credential updated successfully");
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testExecuteWithInvalidCredentialName() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();
        SecureString newValue = new CredentialObject("new_test_password").get();

        // Act
        updateDynamicCredential.execute(
                "invalid_credential_name",
                TEST_ATTRIBUTE_NAME,
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2,
                newValue,
                ""
        );

        // Assert: BotCommandException is expected
    }

    @Test(expectedExceptions = BotCommandException.class)
    public void testExecuteWithInvalidAttributeName() {
        // Arrange
        SecureString username = new CredentialObject(TEST_USERNAME).get();
        SecureString password = new CredentialObject(TEST_PASSWORD).get();
        SecureString crUrl = new CredentialObject(TEST_CR_URL).get();
        SecureString newValue = new CredentialObject("new_test_password").get();

        // Act
        updateDynamicCredential.execute(
                TEST_CREDENTIAL_NAME,
                "invalid_attribute_name",
                AUTH_TYPE_AUTHENTICATE,
                username,
                AUTH_METHOD_PASSWORD,
                password,
                CR_TYPE_SPECIFIC,
                crUrl,
                AUTH_VERSION_V2,
                newValue,
                ""
        );

        // Assert: BotCommandException is expected
    }
}