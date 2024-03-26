/**
 * @author Sumit Kumar
 */
package com.automationanywhere.utilities;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public final class RandomPasswordGenerator {

    public static String generateRandomPassword(int length, int numLower, int numUpper, int numDigits,
                                                int numSpecial, final String allowedSpecial) {
        PasswordGenerator gen = new PasswordGenerator();

        int ruleSize = numLower > 0 ? 1 : 0;
        ruleSize += numUpper > 0 ? 1 : 0;
        ruleSize += numDigits > 0 ? 1 : 0;
        ruleSize += numSpecial > 0 ? 1 : 0;

        CharacterRule[] ruleArr = new CharacterRule[ ruleSize ];
        int currentIndex = 0;

        if (numLower > 0) {
            CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase);
            lowerCaseRule.setNumberOfCharacters(numLower);
            ruleArr[ currentIndex++ ] = lowerCaseRule;
        }

        if (numUpper > 0) {
            CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase);
            upperCaseRule.setNumberOfCharacters(numUpper);
            ruleArr[ currentIndex++ ] = upperCaseRule;
        }

        if (numDigits > 0) {
            CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit);
            digitRule.setNumberOfCharacters(numDigits);
            ruleArr[ currentIndex++ ] = digitRule;
        }

        if (numSpecial > 0) {
            CharacterData specialChars = new CharacterData() {
                public String getErrorCode() {
                    return "ERRONEOUS_SPECIAL_CHARS";
                }

                public String getCharacters() {
                    return allowedSpecial;
                }
            };
            CharacterRule splCharRule = new CharacterRule(specialChars);
            splCharRule.setNumberOfCharacters(numSpecial);
            ruleArr[ currentIndex ] = splCharRule;
        }

        return gen.generatePassword(length, ruleArr);
    }
}
