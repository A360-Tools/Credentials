import com.automationanywhere.botcommand.actions.UpdateDynamicCredential;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.core.security.SecureString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Sumit Kumar
 */
public class UpdateDynamicCredentialTest {
    private final SecureString username = new CredentialObject("myemail@email.com").get();
    private final String authType = "authenticate";  // user or authenticate
    private final String authMethod = "password";  // password or apikey
    private final SecureString authDetails = new CredentialObject("mypass").get();
    private final String CRType = "specific";// current or specific
    private final SecureString specificCRURL = new CredentialObject("https://community.cloud.automationanywhere.digital/").get();// Use if CRType is specific
    private UpdateDynamicCredential credentialUpdater;

    @BeforeClass
    public void setUp() {
        credentialUpdater = new UpdateDynamicCredential();
    }

    @Test
    public void testFetchCredential() throws Exception {
        String credentialName = "test_crd";
        String attributeName = "username";
        SecureString newValue = new CredentialObject("mynewuser1").get();
        credentialUpdater.execute(
                credentialName, attributeName, authType, username, authMethod, authDetails, CRType, specificCRURL, newValue, "");
    }

}