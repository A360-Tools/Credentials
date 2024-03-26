/**
 * @author Sumit Kumar
 */
package com.automationanywhere.botcommand.actions;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.CredentialObject;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.core.security.SecureString;
import com.automationanywhere.utilities.RandomPasswordGenerator;


@BotCommand
@CommandPkg(label = "[[GenerateRandomPassword.label]]", description = "[[GenerateRandomPassword.description]]",
        icon = "credential.svg", name = "GenRandomPassword",
        return_label = "[[GenerateRandomPassword.action.return_label]]",
        node_label = "[[GenerateRandomPassword.action.node_label]]", return_type = DataType.CREDENTIAL,
        return_required = true,
//        allowed_agent_targets = AllowedTarget.HEADLESS,
        documentation_url = "https://github.com/A360-Tools/Credentials/blob/main/src/main/docs/GenerateRandomPassword" +
                ".md")
public class GenerateRandomPassword {
    private static final Messages MESSAGES = MessagesFactory
            .getMessages("com.automationanywhere.botcommand.messages.messages");

    @Execute
    public Value<SecureString> action(
            @Idx(index = "1", type = AttributeType.NUMBER) @Pkg(label = "[[GenerateRandomPassword.length.label]]",
                    default_value_type = DataType.NUMBER, description = "[[GenerateRandomPassword.length.description]]")
            @NotEmpty
            @NumberInteger
            @GreaterThan("0")
            Double length,

            @Idx(index = "2", type = AttributeType.NUMBER) @Pkg(label = "[[GenerateRandomPassword.numLower.label]]",
                    default_value_type = DataType.NUMBER, description = "[[GenerateRandomPassword.numLower" +
                    ".description]]")
            @NotEmpty
            @NumberInteger
            @GreaterThanEqualTo("0")
            Double numLower,

            @Idx(index = "3", type = AttributeType.NUMBER) @Pkg(label = "[[GenerateRandomPassword.numUpper.label]]",
                    default_value_type = DataType.NUMBER, description = "[[GenerateRandomPassword.numUpper" +
                    ".description]]")
            @NotEmpty
            @NumberInteger
            @GreaterThanEqualTo("0")
            Double numUpper,

            @Idx(index = "4", type = AttributeType.NUMBER) @Pkg(label = "[[GenerateRandomPassword.numDigits.label]]",
                    default_value_type = DataType.NUMBER, description = "[[GenerateRandomPassword.numDigits" +
                    ".description]]")
            @NotEmpty
            @NumberInteger
            @GreaterThan("0")
            Double numDigits,

            @Idx(index = "5", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "5.1", pkg = @Pkg(label = "[[GenerateRandomPassword.Include.label]]", value =
                            "include")),
                    @Idx.Option(index = "5.2", pkg = @Pkg(label = "[[GenerateRandomPassword.Exclude.label]]", value =
                            "exclude"))})
            @Pkg(label = "[[GenerateRandomPassword.specialCharactersFlag.label]]",
                    description = "[[GenerateRandomPassword.specialCharactersFlag.description]]",
                    default_value = "include", default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String specialCharactersFlag,

            @Idx(index = "5.1.1", type = AttributeType.TEXT)
            @Pkg(label = "[[GenerateRandomPassword.allowedSpecialCharacters.label]]", default_value = "!@#$%^&*()_+",
                    default_value_type = DataType.STRING)
            @NotEmpty
            String allowedSpecialCharacters,

            @Idx(index = "5.1.2", type = AttributeType.NUMBER) @Pkg(label = "[[GenerateRandomPassword.numSpecial" +
                    ".label]]",
                    default_value_type = DataType.NUMBER,
                    description = "[[GenerateRandomPassword.numSpecial.description]]")
            @NotEmpty
            @NumberInteger
            @GreaterThan("0")
            Double numSpecial
    ) {
        try {
            if (specialCharactersFlag.equals("exclude")) {
                numSpecial = 0.0d;
                allowedSpecialCharacters = "";
            }

            if (length == null || length.intValue() <= 0) {
                throw new BotCommandException(MESSAGES.getString("lengthNegative"));
            }
            if (numLower == null || numLower.intValue() < 0) {
                throw new BotCommandException(MESSAGES.getString("numLowerNegative"));
            }
            if (numUpper == null || numUpper.intValue() < 0) {
                throw new BotCommandException(MESSAGES.getString("numUpperNegative"));
            }
            if (numDigits == null || numDigits.intValue() < 0) {
                throw new BotCommandException(MESSAGES.getString("numDigitsNegative"));
            }
            if (numSpecial == null || numSpecial.intValue() < 0) {
                throw new BotCommandException(MESSAGES.getString("numSpecialNegative"));
            }

            int minimumCharactersLength = (int) (numLower + numUpper + numDigits + numSpecial);

            if (length.intValue() < minimumCharactersLength) {
                throw new BotCommandException(MESSAGES.getString("lengthTooShort"));
            }

            if (specialCharactersFlag.equals("include") && allowedSpecialCharacters.isEmpty()) {
                throw new BotCommandException(MESSAGES.getString("specialCharactersEmpty"));
            }

            String randomPassword = RandomPasswordGenerator.generateRandomPassword(
                    length.intValue(),
                    numLower.intValue(),
                    numUpper.intValue(),
                    numDigits.intValue(),
                    numSpecial.intValue(),
                    allowedSpecialCharacters);
            return new CredentialObject(randomPassword);
        } catch (Exception e) {
            // required to provide proper error message on UI
            throw new BotCommandException(e.toString());
        }
    }
}
