/**
 * @author Sumit Kumar
 */
package com.automationanywhere.utilities;

import com.automationanywhere.botcommand.exception.BotCommandException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public final class HTTPRequest {
    private static final CloseableHttpClient httpClient = HttpClientBuilder
            .create()
            .build();

    public static String request(String url, Map<String, String> headers, String body, HttpMethod method) {
        try {
            HttpRequestBase request = createRequest(url, headers, body, method);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() >= 400) {
                    handleErrorResponse(response);
                }

                return entity != null ? EntityUtils.toString(entity) : null;
            }
        } catch (IOException e) {
            throw new BotCommandException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    private static HttpRequestBase createRequest(String url, Map<String, String> headers, String body,
                                                 HttpMethod method) {
        HttpRequestBase request = getHttpRequestBase(url, headers, method);

        if (body != null && !body.isEmpty()) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntity jsonStringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
                ((HttpEntityEnclosingRequestBase) request).setEntity(jsonStringEntity);
            } else {
                throw new IllegalArgumentException("HTTP method " + method + " does not support entity enclosing.");
            }
        }

        return request;
    }

    private static void handleErrorResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String errorDetails = entity != null ? EntityUtils.toString(entity) : "";
        throw new BotCommandException("Could not complete the action. Error code: " +
                response.getStatusLine().getStatusCode() + " Error details: " + errorDetails);
    }

    @NotNull
    private static HttpRequestBase getHttpRequestBase(String url, Map<String, String> headers, HttpMethod method) {
        HttpRequestBase request;
        switch (method) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                break;
            case PUT:
                request = new HttpPut(url);
                break;
            case PATCH:
                request = new HttpPatch(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        // Set custom headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }
        return request;
    }
}
