## Get Credential Attributes

- **Description:** Retrieves attributes of a credential based on the provided parameters and authentication
  settings.

### Parameters

1. `credentialName` (Type: `String`)
    - **Label:** Credential Name
    - **Description:** The name of the credential to retrieve attributes from.
    - **Constraints:**
        - Must not be empty.

2. `authType` (Type: `String`)
    - **Label:** Authentication Type
    - **Description:** The authentication type.
    - **Default Value:** "Current User"
    - **Options:**
        - Current User: Use current user credentials.
        - Specific User: Authenticate with specific credentials.
    - **Constraints:**
        - Must not be empty.

    - If `authType` is "authenticate" (Specific User), the following parameters become mandatory:

      2.1. `username` (Type: `SecureString`)
        - **Label:** Username
        - **Description:** The username for authentication.
        - **Constraints:**
        - Must not be empty.

      2.2. `authMethod` (Type: `String`)
        - **Label:** Authentication Method
        - **Description:** The authentication method.
        - **Default Value:** "password"
        - **Options:**
        - Password: Authenticate using a password.
        - API Key: Authenticate using an API key.
        - **Constraints:**
        - Must not be empty.

      2.3. `authDetails` (Type: `SecureString`)
        - **Label:** Authentication Details
        - **Description:** The authentication details (password or API key) for authentication.
        - **Constraints:**
        - Must not be empty.

      2.4. `CRType` (Type: `String`)
        - **Label:** Change Request Type
        - **Description:** The change request type.
        - **Default Value:** "current"
        - **Options:**
        - Current CR: Use the current change request.
        - Specific CR: Use a specific change request.
        - **Constraints:**
        - Must not be empty.

            - If `CRType` is "specific" (Specific CR), the following parameter becomes mandatory:

              2.4.1. `specificCRURL` (Type: `SecureString`)
                - **Label:** Specific CR URL
                - **Description:** The URL of the specific change request.
                - **Constraints:**
                - Must not be empty.

- **Return Type:** `Dictionary` keys = attribute ids | values = attribute values

- **Exceptions:** Throws a `BotCommandException` if there are errors during attribute retrieval or processing.
