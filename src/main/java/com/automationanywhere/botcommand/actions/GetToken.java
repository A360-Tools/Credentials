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

import static com.automationanywhere.utilities.AuthenticationUtils.*;

@BotCommand
@CommandPkg(
        label = "[[GetToken.label]]",
        description = "[[GetToken.description]]",
        icon = "credential.svg",
        name = "GetToken",
        return_label = "[[GetToken.return.label]]",
        node_label = "[[GetToken.node.label]]",
        return_type = DataType.CREDENTIAL,
        return_required = true,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/GetToken.md"
)
public class GetToken {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.messages" +
            ".messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    public void setGlobalSessionContext(GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    @Execute
    public Value<SecureString> action(
            @Idx(index = "1", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.1", pkg = @Pkg(label = "[[GetToken.authType.currentUser.label]]", value =
                            AUTH_TYPE_USER)),
                    @Idx.Option(index = "1.2", pkg = @Pkg(label = "[[GetToken.authType.specificUser.label]]", value =
                            AUTH_TYPE_AUTHENTICATE))
            })
            @Pkg(label = "[[GetToken.authType.label]]", description = "[[GetToken.authType.description]]",
                    default_value = AUTH_TYPE_USER, default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String authType,

            @Idx(index = "1.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.username.label]]", description = "[[GetToken.username.description]]")
            @NotEmpty
            @CredentialAllowPassword
            SecureString username,

            @Idx(index = "1.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "1.2.2.1", pkg = @Pkg(label = "[[GetToken.authMethod.password.label]]",
                            value = AUTH_METHOD_PASSWORD)),
                    @Idx.Option(index = "1.2.2.2", pkg = @Pkg(label = "[[GetToken.authMethod.apiKey.label]]", value =
                            AUTH_METHOD_APIKEY))
            })
            @Pkg(label = "[[GetToken.authMethod.label]]", description = "[[GetToken.authMethod.description]]",
                    default_value = AUTH_METHOD_PASSWORD, default_value_type = DataType.STRING)
            @NotEmpty
            String authMethod,

            @Idx(index = "1.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.authDetails.label]]", description = "[[GetToken.authDetails.description]]")
            @NotEmpty
            @CredentialAllowPassword
            SecureString authDetails,

            @Idx(index = "1.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.2.4.1", pkg = @Pkg(label = "[[GetToken.CRType.currentCR.label]]", value =
                            CR_TYPE_CURRENT)),
                    @Idx.Option(index = "1.2.4.2", pkg = @Pkg(label = "[[GetToken.CRType.specificCR.label]]", value =
                            CR_TYPE_SPECIFIC))
            })
            @Pkg(label = "[[GetToken.CRType.label]]", default_value = CR_TYPE_CURRENT, default_value_type =
                    DataType.STRING)
            @NotEmpty
            @SelectModes String crType,

            @Idx(index = "1.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.specificCR.label]]")
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL,

            @Idx(index = "1.2.5", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.2.5.1", pkg = @Pkg(label = "v1", value = AUTH_VERSION_V1)),
                    @Idx.Option(index = "1.2.5.2", pkg = @Pkg(label = "v2", value = AUTH_VERSION_V2))
            })
            @Pkg(label = "Authentication Version", default_value = AUTH_VERSION_V2, default_value_type =
                    DataType.STRING)
            @NotEmpty String authVersion
    ) {
        try {
            String token;
            String crUrl;
            switch (authType) {
                case AUTH_TYPE_USER:
                    token = this.globalSessionContext.getUserToken();
                    break;
                case AUTH_TYPE_AUTHENTICATE:
                    if (crType.equals(CR_TYPE_CURRENT)) {
                        crUrl = globalSessionContext.getCrUrl();
                    } else {
                        crUrl = getSanitziedCRURL(specificCRURL.getInsecureString());
                    }
                    switch (authMethod) {
                        case AUTH_METHOD_PASSWORD:
                            token = CRRequests.withPassword(crUrl, username.getInsecureString(),
                                    authDetails.getInsecureString(), authVersion).getToken();
                            break;
                        case AUTH_METHOD_APIKEY:
                            token = CRRequests.withApiKey(crUrl, username.getInsecureString(),
                                    authDetails.getInsecureString(), authVersion).getToken();
                            break;
                        default:
                            throw new BotCommandException(MESSAGES.getString("invalidAuthMethod", authMethod));
                    }
                    break;
                default:
                    throw new BotCommandException(MESSAGES.getString("invalidAuthType", authType));
            }
            return new CredentialObject(token);
        } catch (Exception e) {
            throw new BotCommandException(e.toString());
        }
    }
}