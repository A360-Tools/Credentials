import com.automationanywhere.botcommand.actions.GetDynamicCredential;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.core.security.SecureString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author Sumit Kumar
 */
public class GetDynamicCredentialTest {
    private final SecureString username = new CredentialObject("myemail@email.com").get();
    private final String authType = "authenticate";  // user or authenticate
    private final String authMethod = "password";  // password or apikey
    private final SecureString authDetails = new CredentialObject("mypass").get();
    private final String CRType = "specific";// current or specific
    private final SecureString specificCRURL = new CredentialObject("https://community.cloud.automationanywhere.digital/").get();// Use if CRType is specific
    private GetDynamicCredential credentialFetcher;

    @BeforeClass
    public void setUp() {
        credentialFetcher = new GetDynamicCredential();
    }

    @Test
    public void testFetchCredential() throws Exception {
        String credentialName = "test_crd";
        String attributeName = "username";
        Value<SecureString> credentialObject = credentialFetcher.execute(
                credentialName, attributeName, authType, username, authMethod, authDetails, CRType, specificCRURL);

        assertNotNull(credentialObject);
        String credentialValue = credentialObject.get().getInsecureString();
        assertNotNull(credentialValue);
        System.out.println(credentialValue);
    }

}