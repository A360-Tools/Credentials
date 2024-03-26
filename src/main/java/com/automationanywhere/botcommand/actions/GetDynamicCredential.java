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
//        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/GetDynamicCredential.md",
        return_type = DataType.CREDENTIAL)
public class GetDynamicCredential {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages" +
            ".messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
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
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "[[GetDynamicCred.authType.currentUser.label]]",
                            value = "user")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "[[GetDynamicCred.authType.specificUser.label]]",
                            value = "authenticate"))})
            @Pkg(label = "[[GetDynamicCred.authType.label]]", description = "[[GetDynamicCred.authType.description]]",
                    default_value = "user", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String authType,

            @Idx(index = "3.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "3.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "3.2.2.1", pkg = @Pkg(label = "[[GetDynamicCred.authMethod.password.label]]",
                            value = "password")),
                    @Idx.Option(index = "3.2.2.2", pkg = @Pkg(label = "[[GetDynamicCred.authMethod.apiKey.label]]",
                            value = "apikey"))})
            @Pkg(label = "[[GetDynamicCred.authMethod.label]]", default_value = "password", default_value_type =
                    DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "3.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "3.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.4.1", pkg = @Pkg(label = "[[GetDynamicCred.CRType.currentCR.label]]",
                            value = "current")),
                    @Idx.Option(index = "3.2.4.2", pkg = @Pkg(label = "[[GetDynamicCred.CRType.specificCR.label]]",
                            value = "specific"))})
            @Pkg(label = "[[GetDynamicCred.CRType.label]]", default_value = "current", default_value_type =
                    DataType.STRING)
            @NotEmpty
            @SelectModes String CRType,

            @Idx(index = "3.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCred.specificCR.label]]", default_value_type = DataType.CREDENTIAL)
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL

    ) {
        try {
            if (credentialName == null || credentialName.isEmpty()) {
                throw new BotCommandException("Credential name is empty");
            }
            if (attributeName == null || attributeName.isEmpty()) {
                throw new BotCommandException("Attribute name is empty");
            }

            String CRURL;
            String TOKEN;
            CRRequests crRequestsObject;
            switch (authType) {
                case "user":
                    CRURL = this.globalSessionContext.getCrUrl();
                    TOKEN = this.globalSessionContext.getUserToken();
                    crRequestsObject = new CRRequests(CRURL, TOKEN);
                    break;
                case "authenticate":
                    CRURL = getCRURL(specificCRURL, CRType);
                    switch (authMethod) {
                        case "password":
                            crRequestsObject = CRRequests
                                    .withPassword(CRURL, username.getInsecureString(), authDetails.getInsecureString());
                            break;
                        case "apikey":
                            crRequestsObject = CRRequests
                                    .withApiKey(CRURL, username.getInsecureString(), authDetails.getInsecureString());
                            break;
                        default:
                            throw new BotCommandException(MESSAGES.getString("invalidAuthMethod", authMethod));
                    }
                    break;
                default:
                    throw new BotCommandException(MESSAGES.getString("invalidAuthType", authType));
            }

            return fetchCredentialAttribute(credentialName, attributeName, crRequestsObject);
        } catch (Exception e) {
            // required to provide proper error message on UI
            throw new BotCommandException(e.toString());
        }

    }

    private String getCRURL(SecureString URL, String CRType) {
        if (CRType.equalsIgnoreCase("current")) {
            return this.globalSessionContext.getCrUrl();
        }

        String expectedURLformat = URL.getInsecureString().replaceAll("/+$", "");
        if (isValidURL(expectedURLformat)) {
            return expectedURLformat;
        } else {
            throw new BotCommandException(MESSAGES.getString("invalidCRURL", URL));
        }
    }

    private CredentialObject fetchCredentialAttribute(String credentialName, String attributeName,
                                                      CRRequests crRequestsObject) {
        String UserId = getUserIdFromToken(crRequestsObject.getToken());
        JSONObject credential = getCredentialByName(crRequestsObject, credentialName);
        String credentialID = credential.getString("id");
        JSONArray attributes = credential.getJSONArray("attributes");
        String credentialAttributeId = getAttributeIdByName(attributes, attributeName);
        String credValue = getAttributeValue(crRequestsObject, credentialID, credentialAttributeId, UserId);

        return new CredentialObject(credValue);
    }

    public static boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private String getUserIdFromToken(String TOKEN) {
        String[] chunks = TOKEN.split("\\.");
        JSONObject payload = new JSONObject(decode(chunks[ 1 ]));
        return payload.getString("sub");
    }

    private JSONObject getCredentialByName(CRRequests crRequestsObject, String credentialName) {
        String response = crRequestsObject.getCredentialByName(credentialName);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray list = responseJSON.getJSONArray("list");

        if (list.isEmpty()) {
            throw new BotCommandException(MESSAGES.getString("invalidCredentialName", credentialName));
        }

        return list.getJSONObject(0);
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

    private String getAttributeValue(CRRequests crRequestsObject, String credentialID, String credentialAttributeId,
                                     String UserId) {
        String response = crRequestsObject.getAttributeValue(credentialID, credentialAttributeId, UserId);
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

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }
}
