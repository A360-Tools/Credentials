package com.automationanywhere.utilities;

import com.automationanywhere.botcommand.exception.BotCommandException;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthenticationUtils {
    public static final String AUTH_TYPE_USER = "user";
    public static final String AUTH_TYPE_AUTHENTICATE = "authenticate";
    public static final String AUTH_METHOD_PASSWORD = "password";
    public static final String AUTH_METHOD_APIKEY = "apikey";
    public static final String CR_TYPE_CURRENT = "current";
    public static final String CR_TYPE_SPECIFIC = "specific";
    public static final String AUTH_VERSION_V1 = "v1";
    public static final String AUTH_VERSION_V2 = "v2";

    public static String getSanitziedCRURL(String specificCRURL) {

        String expectedURLformat = specificCRURL.replaceAll("/+$", "");
        if (isValidURL(expectedURLformat)) {
            return expectedURLformat;
        } else {
            throw new BotCommandException("Invalid CR URL: " + specificCRURL);
        }
    }

    public static boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}