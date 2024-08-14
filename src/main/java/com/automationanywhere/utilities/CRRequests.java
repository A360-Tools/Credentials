package com.automationanywhere.utilities;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CRRequests {
    private static final String AUTHENTICATION_URI_V1 = "/v1/authentication";
    private static final String AUTHENTICATION_URI_V2 = "/v2/authentication";
    private static final String CREDENTIALS_LIST_URI = "/v2/credentialvault/credentials/list";
    private static final String ATTRIBUTE_VALUES_URI = "/v2/credentialvault/credentials/%s/attributevalues";
    private final Map<String, String> headers;
    private final String CRURL;
    private final String token;

    public CRRequests(String CRURL, String token) {
        this.CRURL = CRURL;
        this.token = token;
        headers = createCommonHeaders();
        headers.put("X-Authorization", token);
    }

    private static Map<String, String> createCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public static CRRequests withPassword(String CRURL, String username, String password, String version) {
        String token = getTokenByPassword(CRURL, username, password, version);
        return new CRRequests(CRURL, token);
    }

    private static String getTokenByPassword(String CRURL, String username, String password, String version) {
        String authURI = CRURL + (version.equals("v2") ? AUTHENTICATION_URI_V2 : AUTHENTICATION_URI_V1);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("username", username);
        jsonBody.put("password", password);
        String response = HTTPRequest.request(authURI, createCommonHeaders(), String.valueOf(jsonBody),
                HttpMethod.POST);
        Objects.requireNonNull(response);
        return new JSONObject(response).getString("token");
    }

    public static CRRequests withApiKey(String CRURL, String username, String apikey, String version) {
        String token = getTokenByKey(CRURL, username, apikey, version);
        return new CRRequests(CRURL, token);
    }

    private static String getTokenByKey(String CRURL, String username, String apikey, String version) {
        String authURI = CRURL + (version.equals("v2") ? AUTHENTICATION_URI_V2 : AUTHENTICATION_URI_V1);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("username", username);
        jsonBody.put("apiKey", apikey);
        String response = HTTPRequest.request(authURI, createCommonHeaders(), String.valueOf(jsonBody),
                HttpMethod.POST);
        Objects.requireNonNull(response);
        return new JSONObject(response).getString("token");
    }

    public String getToken() {
        return token;
    }

    public String getCredentialByName(String credentialName) {
        String listCredentialURI = CRURL + CREDENTIALS_LIST_URI;
        JSONObject jsonBody = new JSONObject();
        JSONObject filter = new JSONObject();
        filter.put("operator", "eq");
        filter.put("field", "name");
        filter.put("value", credentialName);
        jsonBody.put("filter", filter);
        return HTTPRequest.request(listCredentialURI, headers, jsonBody.toString(), HttpMethod.POST);
    }

    public String getAttributeValueIDByCredential(String credentialID) {
        String attributeValuesURI = String.format(CRURL + ATTRIBUTE_VALUES_URI, credentialID);
        return HTTPRequest.request(attributeValuesURI, headers, null, HttpMethod.GET);
    }

    public String updateAttributeValue(String credentialID, String credentialAttributeValueId, String updatedValue,
                                       String updatedVersion) {
        String updateAttributeValueURI =
                String.format(CRURL + ATTRIBUTE_VALUES_URI, credentialID) + "/" + credentialAttributeValueId;
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("value", updatedValue);
        jsonBody.put("version", updatedVersion);
        return HTTPRequest.request(updateAttributeValueURI, headers, jsonBody.toString(), HttpMethod.PUT);
    }

    public String getAttributeValue(String credentialID, String credentialAttributeValueId, String UserId) {
        String updateAttributeValueURI = String.format(CRURL + ATTRIBUTE_VALUES_URI, credentialID) +
                "?credentialAttributeId=" + credentialAttributeValueId + "&userId=" + UserId;
        return HTTPRequest.request(updateAttributeValueURI, headers, null, HttpMethod.GET);
    }
}
