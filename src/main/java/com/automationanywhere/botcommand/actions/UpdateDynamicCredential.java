package com.automationanywhere.botcommand.actions;

import com.automationanywhere.bot.service.GlobalSessionContext;
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

import static com.automationanywhere.utilities.AuthenticationUtils.*;

@BotCommand
@CommandPkg(
        label = "[[UpdateDynamicCred.label]]",
        description = "[[UpdateDynamicCred.description]]",
        icon = "credential.svg",
        name = "UpdateDynamicCred",
        node_label = "[[UpdateDynamicCred.node_label]]",
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs" +
                "/UpdateDynamicCredential.md"
)
public class UpdateDynamicCredential {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages" +
            ".messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    @Execute
    public void execute(
            @Idx(index = "1", type = AttributeType.TEXT)
            @Pkg(label = "[[UpdateDynamicCred.credentialName.label]]", description = "[[UpdateDynamicCred" +
                    ".credentialName.description]]")
            @NotEmpty String credentialName,

            @Idx(index = "2", type = AttributeType.TEXT)
            @Pkg(label = "[[UpdateDynamicCred.attributeName.label]]", description = "[[UpdateDynamicCred" +
                    ".attributeName.description]]")
            @NotEmpty String attributeName,

            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "[[UpdateDynamicCred.authType.currentUser.label]]",
                            value = AUTH_TYPE_USER)),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "[[UpdateDynamicCred.authType.specificUser.label]]"
                            , value = AUTH_TYPE_AUTHENTICATE))
            })
            @Pkg(label = "[[UpdateDynamicCred.authType.label]]", description = "[[UpdateDynamicCred.authType" +
                    ".description]]", default_value = AUTH_TYPE_USER, default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String authType,

            @Idx(index = "3.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "3.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "3.2.2.1", pkg = @Pkg(label = "[[UpdateDynamicCred.authMethod.password" +
                            ".label]]", value = AUTH_METHOD_PASSWORD)),
                    @Idx.Option(index = "3.2.2.2", pkg = @Pkg(label = "[[UpdateDynamicCred.authMethod.apiKey.label]]"
                            , value = AUTH_METHOD_APIKEY))
            })
            @Pkg(label = "[[UpdateDynamicCred.authMethod.label]]", default_value = AUTH_METHOD_PASSWORD,
                    default_value_type = DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "3.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "3.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.4.1", pkg = @Pkg(label = "[[UpdateDynamicCred.CRType.currentCR.label]]",
                            value = CR_TYPE_CURRENT)),
                    @Idx.Option(index = "3.2.4.2", pkg = @Pkg(label = "[[UpdateDynamicCred.CRType.specificCR.label]]"
                            , value = CR_TYPE_SPECIFIC))
            })
            @Pkg(label = "[[UpdateDynamicCred.CRType.label]]", default_value = CR_TYPE_CURRENT, default_value_type =
                    DataType.STRING)
            @NotEmpty
            @SelectModes String crType,

            @Idx(index = "3.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.specificCR.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL,

            @Idx(index = "3.2.5", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.2.5.1", pkg = @Pkg(label = "v1", value = AUTH_VERSION_V1)),
                    @Idx.Option(index = "3.2.5.2", pkg = @Pkg(label = "v2", value = AUTH_VERSION_V2))
            })
            @Pkg(label = "Authentication Version", default_value = AUTH_VERSION_V2, default_value_type =
                    DataType.STRING)
            @NotEmpty String authVersion,

            @Idx(index = "4", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[UpdateDynamicCred.newValue.label]]", description = "[[UpdateDynamicCred.newValue" +
                    ".description]]")
            @NotEmpty SecureString updatedValue,

            @Idx(index = "5", type = AttributeType.HELP)
            @Pkg(label = "[[UpdateDynamicCred.help.label]]", description = "[[UpdateDynamicCred.help.description]]")
            @Inject
            String help
    ) {
        try {
            if (credentialName == null || credentialName.isEmpty()) {
                throw new BotCommandException("Credential name is empty");
            }
            if (attributeName == null || attributeName.isEmpty()) {
                throw new BotCommandException("Attribute name is empty");
            }

            CRRequests crRequestsObject;
            String crUrl;
            switch (authType) {
                case AUTH_TYPE_USER:
                    crUrl = this.globalSessionContext.getCrUrl();
                    String token = this.globalSessionContext.getUserToken();
                    crRequestsObject = new CRRequests(crUrl, token);
                    break;
                case AUTH_TYPE_AUTHENTICATE:
                    if (crType.equals(CR_TYPE_CURRENT)) {
                        crUrl = globalSessionContext.getCrUrl();
                    } else {
                        crUrl = getSanitziedCRURL(specificCRURL.getInsecureString());
                    }
                    switch (authMethod) {
                        case AUTH_METHOD_PASSWORD:
                            crRequestsObject = CRRequests.withPassword(crUrl, username.getInsecureString(),
                                    authDetails.getInsecureString(), authVersion);
                            break;
                        case AUTH_METHOD_APIKEY:
                            crRequestsObject = CRRequests.withApiKey(crUrl, username.getInsecureString(),
                                    authDetails.getInsecureString(), authVersion);
                            break;
                        default:
                            throw new BotCommandException(MESSAGES.getString("invalidAuthMethod", authMethod));
                    }
                    break;
                default:
                    throw new BotCommandException(MESSAGES.getString("invalidAuthType", authType));
            }

            updateCredential(crRequestsObject, credentialName, attributeName, updatedValue.getInsecureString());
        } catch (Exception e) {
            throw new BotCommandException(e.toString());
        }
    }

    private void updateCredential(CRRequests crRequestsObject, String credentialName, String attributeName,
                                  String updatedValue) {
        JSONObject credential = getCredentialByName(crRequestsObject, credentialName);
        String credentialID = credential.getString("id");
        JSONArray attributes = credential.getJSONArray("attributes");
        String credentialAttributeId = getAttributeIdByName(attributes, attributeName);
        JSONObject credentialProperty = getAttributeValueProperty(crRequestsObject, credentialID,
                credentialAttributeId);
        String credentialAttributeValueId = credentialProperty.getString("credentialAttributeValueId");
        String credentialAttributeVersion = credentialProperty.getString("version");

        crRequestsObject.updateAttributeValue(credentialID, credentialAttributeValueId, updatedValue,
                credentialAttributeVersion);
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

    private JSONObject getAttributeValueProperty(CRRequests crRequestsObject, String credentialID,
                                                 String credentialAttributeId) {
        String response = crRequestsObject.getAttributeValueIDByCredential(credentialID);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray list = responseJSON.getJSONArray("list");
        for (int i = 0; i < list.length(); i++) {
            JSONObject currentAttribute = list.getJSONObject(i);
            if (currentAttribute.getString("credentialAttributeId").equals(credentialAttributeId)) {
                JSONObject result = new JSONObject();
                result.put("credentialAttributeValueId", currentAttribute.getString("id"));
                result.put("version", currentAttribute.getString("version"));
                return result;
            }
        }
        throw new BotCommandException(MESSAGES.getString("invalidAttributeValue"));
    }
}