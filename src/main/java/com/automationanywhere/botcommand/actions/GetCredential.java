/**
 * @author Sumit Kumar
 */
package com.automationanywhere.botcommand.actions;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.CredentialAllowPassword;
import com.automationanywhere.commandsdk.annotations.rules.CredentialOnly;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.core.security.SecureString;

//Credential to Text
@BotCommand
@CommandPkg(label = "[[GetCredential.label]]", description = "[[GetCredential.description]]", icon = "credential.svg",
        name = "GetCred", return_label = "[[GetCredential.action.return_label]]",
        node_label = "[[GetCredential.action.node_label]]", return_type = DataType.STRING, return_required = true,
//        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/GetCredential.md")
public class GetCredential {
    private static final Messages MESSAGES = MessagesFactory
            .getMessages("com.automationanywhere.botcommand.messages.messages");

    @Execute
    public Value<String> action(@Idx(index = "1", type = AttributeType.CREDENTIAL)
                                @Pkg(label = "[[GetCredential.action.label]]")
                                @NotEmpty
                                @CredentialOnly
                                @CredentialAllowPassword SecureString credentials) throws Exception {

        int retryLimit = 3;
        for (int retry = 0; retry <= retryLimit; retry++) {
            try {
                String result = credentials.getInsecureString();
                return new StringValue(result);
            } catch (Exception e) {
                if (retry == retryLimit) {
                    throw new BotCommandException(MESSAGES.getString("credentialFetchError", e.getMessage()));
                } else {
                    Thread.sleep(retry * 2000L);
                }
            }
        }

        return new StringValue();
    }
}

