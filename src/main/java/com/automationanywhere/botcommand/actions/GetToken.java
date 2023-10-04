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

import java.net.MalformedURLException;
import java.net.URL;


@BotCommand
@CommandPkg(label = "[[GetToken.label]]", description = "[[GetToken.description]]", icon = "credential.svg", name = "GetToken",
        return_label = "[[GetToken.return.label]]", node_label = "[[GetToken.node.label]]", return_type = DataType.CREDENTIAL,
        return_required = true,
        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/GetToken.md")
public class GetToken {
    private static final Messages MESSAGES = MessagesFactory
            .getMessages("com.automationanywhere.botcommand.messages.messages");
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

    @Execute
    public Value<SecureString> action(
            @Idx(index = "1", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.1", pkg = @Pkg(label = "[[GetToken.authType.currentUser.label]]", value = "user")),
                    @Idx.Option(index = "1.2", pkg = @Pkg(label = "[[GetToken.authType.specificUser.label]]", value = "authenticate"))})
            @Pkg(label = "[[GetToken.authType.label]]", description = "[[GetToken.authType.description]]",
                    default_value = "user", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String authType,

            @Idx(index = "1.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.username.label]]", description = "[[GetToken.username.description]]")
            @NotEmpty
            @CredentialAllowPassword
            SecureString username,

            @Idx(index = "1.2.2", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "1.2.2.1", pkg = @Pkg(label = "[[GetToken.authMethod.password.label]]", value = "password")),
                    @Idx.Option(index = "1.2.2.2", pkg = @Pkg(label = "[[GetToken.authMethod.apiKey.label]]", value = "apikey"))})
            @Pkg(label = "[[GetToken.authMethod.label]]", description = "[[GetToken.authMethod.description]]",
                    default_value = "password", default_value_type = DataType.STRING)
            @NotEmpty
            String authMethod,

            @Idx(index = "1.2.3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.authDetails.label]]", description = "[[GetToken.authDetails.description]]")
            @NotEmpty
            @CredentialAllowPassword
            SecureString authDetails,

            @Idx(index = "1.2.4", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.2.4.1", pkg = @Pkg(label = "[[GetToken.CRType.currentCR.label]]", value = "current")),
                    @Idx.Option(index = "1.2.4.2", pkg = @Pkg(label = "[[GetToken.CRType.specificCR.label]]", value = "specific"))})
            @Pkg(label = "[[GetToken.CRType.label]]", default_value = "current", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes String CRType,

            @Idx(index = "1.2.4.2.1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[GetToken.specificCR.label]]", default_value_type = DataType.CREDENTIAL)
            @NotEmpty
            @CredentialAllowPassword SecureString specificCRURL) {
        try {
            String CRURL;
            String TOKEN;

            switch (authType) {
                case "user":
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
                            throw new BotCommandException(MESSAGES.getString("invalidAuthMethod", "authMethod"));
                    }
                    break;
                default:
                    throw new BotCommandException(MESSAGES.getString("invalidAuthType", "authType"));
            }
            return new CredentialObject(TOKEN);
        } catch (Exception e) {
            // required to provide proper error message on UI
            throw new BotCommandException(e.toString());
        }


    }
}
