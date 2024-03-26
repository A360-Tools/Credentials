/**
 * @author Sumit Kumar
 */
package com.automationanywhere.botcommand.actions;

import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
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
import java.util.HashMap;
import java.util.Map;

@BotCommand
@CommandPkg(label = "[[GetDynamicCredAttributes.label]]", description = "[[GetDynamicCredAttributes.description]]",
        icon = "credential.svg", name = "GetDynamicCredAttributes",
        node_label = "[[GetDynamicCredAttributes.node_label]]",
        return_label = "[[GetDynamicCredAttributes.return_label]]", return_Direct = true, return_required = true,
//        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs" +
                "/GetDynamicCredentialAttributes.md",
        return_type = DataType.DICTIONARY, return_sub_type = DataType.STRING)
public class GetDynamicCredentialAttributes {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages" +
            ".messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    @Execute
    public DictionaryValue execute(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(label = "[[GetDynamicCredAttributes.credentialName.label]]", default_value_type = DataType.STRING,
                    description = "[[GetDynamicCredAttributes.credentialName.description]]")
            @NotEmpty String credentialName,


            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authType.currentUser" +
                            ".label]]", value = "user")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authType.specificUser" +
                            ".label]]", value = "authenticate"))})
            @Pkg(label = "[[GetDynamicCredAttributes.authType.label]]", description = "[[GetDynamicCredAttributes" +
                    ".authType.description]]",
                    default_value = "user", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String authType,

            @Idx(index = "3.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "3.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "3.2.2.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authMethod.password" +
                            ".label]]", value = "password")),
                    @Idx.Option(index = "3.2.2.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authMethod.apiKey" +
                            ".label]]", value = "apikey"))})
            @Pkg(label = "[[GetDynamicCred.authMethod.label]]", default_value = "password", default_value_type =
                    DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "3.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "3.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.4.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.CRType.currentCR" +
                            ".label]]", value = "current")),
                    @Idx.Option(index = "3.2.4.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.CRType.specificCR" +
                            ".label]]", value = "specific"))})
            @Pkg(label = "[[GetDynamicCredAttributes.CRType.label]]", default_value = "current", default_value_type =
                    DataType.STRING)
            @NotEmpty
            @SelectModes String CRType,

            @Idx(index = "3.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.attributeName.label]]", default_value_type = DataType.CREDENTIAL,
                    description = "[[GetDynamicCredAttributes.attributeName.description]]")
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL

    ) {
        try {
            if (credentialName == null || credentialName.isEmpty()) {
                throw new BotCommandException("Credential name is empty");
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

            return fetchCredentialAttributes(credentialName, crRequestsObject);
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

    private DictionaryValue fetchCredentialAttributes(String credentialName, CRRequests crRequestsObject) {
        JSONObject credential = getCredentialByName(crRequestsObject, credentialName);
        JSONArray attributes = credential.getJSONArray("attributes");
        Map<String, Value> attributesMap = new HashMap<>();
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject currentAttribute = attributes.getJSONObject(i);
            attributesMap.put(currentAttribute.getString("id"), new StringValue(currentAttribute.getString("name")));
        }
        return new DictionaryValue(attributesMap);
    }

    public static boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
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
}
