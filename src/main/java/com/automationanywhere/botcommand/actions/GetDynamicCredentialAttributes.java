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

import java.util.HashMap;
import java.util.Map;

import static com.automationanywhere.utilities.AuthenticationUtils.*;

@BotCommand
@CommandPkg(
        label = "[[GetDynamicCredAttributes.label]]",
        description = "[[GetDynamicCredAttributes.description]]",
        icon = "credential.svg",
        name = "GetDynamicCredAttributes",
        return_label = "[[GetDynamicCredAttributes.return_label]]",
        return_type = DataType.DICTIONARY,
        return_sub_type = DataType.STRING,
        return_required = true
)
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
            @Pkg(label = "[[GetDynamicCredAttributes.credentialName.label]]", description =
                    "[[GetDynamicCredAttributes.credentialName.description]]")
            @NotEmpty String credentialName,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authType.currentUser" +
                            ".label]]", value = AUTH_TYPE_USER)),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authType.specificUser" +
                            ".label]]", value = AUTH_TYPE_AUTHENTICATE))
            })
            @Pkg(label = "[[GetDynamicCredAttributes.authType.label]]", default_value = AUTH_TYPE_USER,
                    default_value_type = DataType.STRING)
            @SelectModes
            @NotEmpty String authType,

            @Idx(index = "2.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.username.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString username,

            @Idx(index = "2.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "2.2.2.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authMethod.password" +
                            ".label]]", value = AUTH_METHOD_PASSWORD)),
                    @Idx.Option(index = "2.2.2.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.authMethod.apiKey" +
                            ".label]]", value = AUTH_METHOD_APIKEY))
            })
            @Pkg(label = "[[GetDynamicCredAttributes.authMethod.label]]", default_value = AUTH_METHOD_PASSWORD,
                    default_value_type = DataType.STRING)
            @NotEmpty String authMethod,

            @Idx(index = "2.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.authDetails.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString authDetails,

            @Idx(index = "2.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.2.4.1", pkg = @Pkg(label = "[[GetDynamicCredAttributes.CRType.currentCR" +
                            ".label]]", value = CR_TYPE_CURRENT)),
                    @Idx.Option(index = "2.2.4.2", pkg = @Pkg(label = "[[GetDynamicCredAttributes.CRType.specificCR" +
                            ".label]]", value = CR_TYPE_SPECIFIC))
            })
            @Pkg(label = "[[GetDynamicCredAttributes.CRType.label]]", default_value = CR_TYPE_CURRENT,
                    default_value_type = DataType.STRING)
            @SelectModes
            @NotEmpty String crType,

            @Idx(index = "2.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetDynamicCredAttributes.specificCR.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL,

            @Idx(index = "2.2.5", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.2.5.1", pkg = @Pkg(label = "v1", value = AUTH_VERSION_V1)),
                    @Idx.Option(index = "2.2.5.2", pkg = @Pkg(label = "v2", value = AUTH_VERSION_V2))
            })
            @Pkg(label = "Authentication Version", default_value = AUTH_VERSION_V2, default_value_type =
                    DataType.STRING)
            @NotEmpty String authVersion
    ) {
        try {
            if (credentialName == null || credentialName.isEmpty()) {
                throw new BotCommandException("Credential name is empty");
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

            return fetchCredentialAttributes(credentialName, crRequestsObject);
        } catch (Exception e) {
            throw new BotCommandException(e.toString());
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