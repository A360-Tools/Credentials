### Generate Random Password

- **Description:** Generates a random password based on specified criteria.

![Alt text](./Screenshots/GenerateRandomPassword.png)

### Parameters

1. `length` (Type: `Number`)
    - **Label:** Length
    - **Description:** The length of the random password.
    - **Constraints:**
        - Must be an integer greater than 0.

2. `numLower` (Type: `Number`)
    - **Label:** Number of Lowercase Characters
    - **Description:** The number of lowercase characters in the password.
    - **Constraints:**
        - Must be an integer greater than or equal to 0.

3. `numUpper` (Type: `Number`)
    - **Label:** Number of Uppercase Characters
    - **Description:** The number of uppercase characters in the password.
    - **Constraints:**
        - Must be an integer greater than or equal to 0.

4. `numDigits` (Type: `Number`)
    - **Label:** Number of Digits
    - **Description:** The number of digits in the password.
    - **Constraints:**
        - Must be an integer greater than 0.

5. `specialCharactersFlag` (Type: `Select`)
    - **Label:** Special Characters Flag
    - **Description:** Flag to include or exclude special characters in the password.
    - **Default Value:** "include"
    - **Options:**
        - Include: Include special characters.
        - Exclude: Exclude special characters.
    - **Constraints:**
        - Must not be empty.

    - If `specialCharactersFlag` is "include," the following parameters become mandatory:

      5.1. `allowedSpecialCharacters` (Type: `Text`)
        - **Label:** Allowed Special Characters
        - **Description:** The special characters to include in the password.
        - **Default Value:** "!@#$%^&*()_+"
        - **Constraints:**
        - Must not be empty.

      5.2. `numSpecial` (Type: `Number`)
        - **Label:** Number of Special Characters
        - **Description:** The number of special characters to include in the password.
        - **Constraints:**
        - Must be an integer greater than 0.

- **Return Type:** `Credential`

- **Exceptions:** Throws a `BotCommandException` if there are errors during password generation or validation.
