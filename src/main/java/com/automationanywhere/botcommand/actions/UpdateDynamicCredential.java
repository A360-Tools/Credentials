/**
 * @author Sumit Kumar
 */
package com.automationanywhere.botcommand.actions;

import com.automationanywhere.bot.service.GlobalSessionContext;
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
import java.util.HashMap;
import java.util.Map;

@BotCommand
@CommandPkg(label = "[[UpdateDynamicCred.label]]", description = "[[UpdateDynamicCred.description]]",
        icon = "credential.svg", name = "UpdateDynamicCred",
        node_label = "[[UpdateDynamicCred.node_label]]",
        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/UpdateDynamicCredential.md")
public class UpdateDynamicCredential {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages.messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

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

    private String getCRURL(SecureString URL, String CRType) {
        if (CRType.equalsIgnoreCase("current"))
            return this.globalSessionContext.getCrUrl();

        String expectedURLformat = URL.getInsecureString().replaceAll("/+$", "");
        if (isValidURL(expectedURLformat))
            return expectedURLformat;
        else
            throw new BotCommandException(MESSAGES.getString("invalidCRURL", URL));
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

    private Map<String, String> getAttributeValueProperty(CRRequests crRequestsObject, String credentialID, String credentialAttributeId) {
        String response = crRequestsObject.getAttributeValueIDByCredential(credentialID);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray list = responseJSON.getJSONArray("list");
        Map<String, String> credentialProperty = new HashMap<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject currentAttribute = list.getJSONObject(i);

            if (currentAttribute.getString("credentialAttributeId").equals(credentialAttributeId)) {
                credentialProperty.put("credentialAttributeValueId", currentAttribute.getString("id"));
                credentialProperty.put("version", currentAttribute.getString("version"));
                return credentialProperty;
            }
        }

        throw new BotCommandException(MESSAGES.getString("invalidAttributeValue"));
    }

    @Execute
    public void execute(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(label = "[[UpdateDynamicCred.credentialName.label]]", default_value_type = DataType.STRING,
                    description = "[[UpdateDynamicCred.credentialName.description]]")
            @NotEmpty String credentialName,

            @Idx(index = "2", type = AttributeType.TEXT)
            @Pkg(label = "[[UpdateDynamicCred.attributeName.label]]", default_value_type = DataType.STRING,
                    description = "[[UpdateDynamicCred.attributeName.description]]")
            @NotEmpty String attributeName,

            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "[[UpdateDynamicCred.authType.currentUser.label]]", value = "user")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "[[UpdateDynamicCred.authType.specificUser.label]]", value = "authenticate"))})
            @Pkg(label = "[[UpdateDynamicCred.authType.label]]", description = "[[UpdateDynamicCred.authType.description]]",
                    default_value = "user", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String authType,

            @Idx(index = "3.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "3.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "3.2.2.1", pkg = @Pkg(label = "[[UpdateDynamicCred.authMethod.password.label]]", value = "password")),
                    @Idx.Option(index = "3.2.2.2", pkg = @Pkg(label = "[[UpdateDynamicCred.authMethod.apiKey.label]]", value = "apikey"))})
            @Pkg(label = "[[UpdateDynamicCred.authMethod.label]]", default_value = "password", default_value_type = DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "3.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "3.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.4.1", pkg = @Pkg(label = "[[UpdateDynamicCred.CRType.currentCR.label]]", value = "current")),
                    @Idx.Option(index = "3.2.4.2", pkg = @Pkg(label = "[[UpdateDynamicCred.CRType.specificCR.label]]", value = "specific"))})
            @Pkg(label = "[[UpdateDynamicCred.CRType.label]]", default_value = "current", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String CRType,

            @Idx(index = "3.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.specificCR.label]]", default_value_type = DataType.CREDENTIAL)
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL,

            @Idx(index = "4", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.newValue.label]]", default_value_type = DataType.CREDENTIAL,
                    description = "[[UpdateDynamicCred.newValue.description]]")
            @NotEmpty SecureString updatedValue,

            @Idx(index = "5", type = AttributeType.HELP)
            @Pkg(label = "[[UpdateDynamicCred.help.label]]", description = "[[UpdateDynamicCred.help.description]]")
            @Inject
            String help

    ) {
        try {
            if (credentialName == null || credentialName.isEmpty())
                throw new BotCommandException("Credential name is empty");
            if (attributeName == null || attributeName.isEmpty())
                throw new BotCommandException("Attribute name is empty");

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

            JSONObject credential = getCredentialByName(crRequestsObject, credentialName);
            String credentialID = credential.getString("id");
            JSONArray attributes = credential.getJSONArray("attributes");
            String credentialAttributeId = getAttributeIdByName(attributes, attributeName);
            Map<String, String> credentialProperty = getAttributeValueProperty(crRequestsObject, credentialID,
                    credentialAttributeId);
            String credentialAttributeValueId = credentialProperty.get("credentialAttributeValueId");
            String credentialAttributeVersion = credentialProperty.get("version");

            crRequestsObject
                    .updateAttributeValue(credentialID, credentialAttributeValueId,
                            updatedValue.getInsecureString(), credentialAttributeVersion);
        } catch (Exception e) {
            // required to provide proper error message on UI
            throw new BotCommandException(e.toString());
        }

    }
}
