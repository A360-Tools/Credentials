/**
 * @author Sumit Kumar
 */
package com.automationanywhere.botcommand.actions;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.CredentialAllowPassword;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.annotations.rules.SelectModes;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.commandsdk.model.AllowedTarget;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.core.security.SecureString;
import com.automationanywhere.utilities.CRRequests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

@BotCommand
@CommandPkg(label = "[[GetDynamicCred.label]]", description = "[[GetDynamicCred.description]]",
        icon = "credential.svg", name = "GetDynamicCred",
        node_label = "[[GetDynamicCred.node_label]]",
        return_label = "[[GetDynamicCred.return_label]]", return_required = true,
        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "",
        return_type = DataType.CREDENTIAL)
public class GetDynamicCredential {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages.messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    public static boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    private CredentialObject fetchCredentialAttribute(String credentialName, String attributeName, String CRURL, String TOKEN) {
        String UserId = getUserIdFromToken(TOKEN);
        JSONObject credential = getCredentialByName(CRURL, TOKEN, credentialName);
        String credentialID = credential.getString("id");
        JSONArray attributes = credential.getJSONArray("attributes");
        String credentialAttributeId = getAttributeIdByName(attributes, attributeName);
        String credValue = getAttributeValue(CRURL, TOKEN, credentialID, credentialAttributeId, UserId);

        return new CredentialObject(credValue);
    }

    private String getUserIdFromToken(String TOKEN) {
        String[] chunks = TOKEN.split("\\.");
        JSONObject payload = new JSONObject(decode(chunks[1]));
        return payload.getString("sub");
    }

    private JSONObject getCredentialByName(String CRURL, String TOKEN, String credentialName) {
        String response = new CRRequests(CRURL, TOKEN).getCredentialByName(credentialName);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray list = responseJSON.getJSONArray("list");

        if (list.isEmpty()) {
            throw new BotCommandException(MESSAGES.getString("invalidCredentialName", credentialName));
        }

        return list.getJSONObject(0);
    }

    private String getCRURL(SecureString URL, String CRType) {
        if (CRType.equalsIgnoreCase("current"))
            return this.globalSessionContext.getCrUrl();

        String expectedURLformat = URL.getInsecureString().replaceAll("/+$", "");
        if (isValidURL(expectedURLformat))
            return expectedURLformat;
        else
            throw new BotCommandException(MESSAGES.getString("invalidCRURL", URL));
    }

    private String getAttributeIdByName(JSONArray attributes, String attributeName) {
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject currentAttribute = attributes.getJSONObject(i);
            if (currentAttribute.getString("name").equals(attributeName)) {
                return currentAttribute.getString("id");
            }
        }
        throw new BotCommandException(MESSAGES.getString("invalidAttributeName", attributeName));
    }

    private String getAttributeValue(String CRURL, String TOKEN, String credentialID, String credentialAttributeId, String UserId) {
        String response = new CRRequests(CRURL, TOKEN).getAttributeValue(credentialID, credentialAttributeId, UserId);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray list = responseJSON.getJSONArray("list");

        for (int i = 0; i < list.length(); i++) {
            JSONObject currentAttribute = list.getJSONObject(i);

            if (currentAttribute.getString("credentialAttributeId").equals(credentialAttributeId)) {
                return currentAttribute.getString("value");
            }
        }

        throw new BotCommandException(MESSAGES.getString("invalidAttributeValue"));
    }

    @Execute
    public Value<SecureString> execute(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(label = "[[GetDynamicCred.credentialName.label]]", default_value_type = DataType.STRING,
                    description = "[[GetDynamicCred.credentialName.description]]")
            @NotEmpty String credentialName,

            @Idx(index = "2", type = AttributeType.TEXT)
            @Pkg(label = "[[GetDynamicCred.attributeName.label]]", default_value_type = DataType.STRING,
                    description = "[[GetDynamicCred.attributeName.description]]")
            @NotEmpty String attributeName,

            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "[[GetDynamicCred.authType.currentUser.label]]", value = "user")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "[[GetDynamicCred.authType.specificUser.label]]", value = "authenticate"))})
            @Pkg(label = "[[GetDynamicCred.authType.label]]", description = "[[GetDynamicCred.authType.description]]",
                    default_value = "user", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String authType,

            @Idx(index = "3.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "3.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "3.2.2.1", pkg = @Pkg(label = "[[GetDynamicCred.authMethod.password.label]]", value = "password")),
                    @Idx.Option(index = "3.2.2.2", pkg = @Pkg(label = "[[GetDynamicCred.authMethod.apiKey.label]]", value = "apikey"))})
            @Pkg(label = "[[GetDynamicCred.authMethod.label]]", default_value = "password", default_value_type = DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "3.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "3.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.4.1", pkg = @Pkg(label = "[[GetDynamicCred.CRType.currentCR.label]]", value = "current")),
                    @Idx.Option(index = "3.2.4.2", pkg = @Pkg(label = "[[GetDynamicCred.CRType.specificCR.label]]", value = "specific"))})
            @Pkg(label = "[[GetDynamicCred.CRType.label]]", default_value = "current", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String CRType,

            @Idx(index = "3.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.specificCR.label]]", default_value_type = DataType.CREDENTIAL)
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL

    ) throws Exception {
        if (credentialName == null || credentialName.isEmpty())
            throw new BotCommandException("Credential name is empty");
        if (attributeName == null || attributeName.isEmpty())
            throw new BotCommandException("Attribute name is empty");

        if (CRType.equalsIgnoreCase("specific") && specificCRURL == null)
            throw new BotCommandException("Control Room URL is required for specific control room");

        String CRURL;
        String TOKEN;

        switch (authType) {
            case "user":
                CRURL = this.globalSessionContext.getCrUrl();
                TOKEN = this.globalSessionContext.getUserToken();
                break;
            case "authenticate":
                CRURL = getCRURL(specificCRURL, CRType);
                switch (authMethod) {
                    case "password":
                        TOKEN = CRRequests
                                .withPassword(CRURL, username.getInsecureString(), authDetails.getInsecureString())
                                .getToken();
                        break;
                    case "apikey":
                        TOKEN = CRRequests
                                .withApiKey(CRURL, username.getInsecureString(), authDetails.getInsecureString())
                                .getToken();
                        break;
                    default:
                        throw new BotCommandException(MESSAGES.getString("invalidAuthMethod", authMethod));
                }
                break;
            default:
                throw new BotCommandException(MESSAGES.getString("invalidAuthType", authType));
        }

        return fetchCredentialAttribute(credentialName, attributeName, CRURL, TOKEN);
    }
}
