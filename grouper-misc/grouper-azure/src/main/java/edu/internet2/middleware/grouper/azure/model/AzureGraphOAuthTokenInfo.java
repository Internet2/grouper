package edu.internet2.middleware.grouper.azure.model;

import com.squareup.moshi.Json;

public class AzureGraphOAuthTokenInfo {
    @Json(name = "token_type") public final String tokenType;
    @Json(name = "scope") public final String scope;
    @Json(name = "expires_in") public final int expiresIn;
    @Json(name = "expires_on") public final int expiresOn;
    @Json(name = "not_before") public final int notBefore;
    @Json(name = "resource") public final String resource;
    @Json(name = "access_token") public final String accessToken;

    public AzureGraphOAuthTokenInfo(String tokenType, String scope, int expiresIn, int expiresOn, int notBefore, String resource, String accessToken) {
        this.tokenType = tokenType;
        this.scope = scope;
        this.expiresIn = expiresIn;
        this.expiresOn = expiresOn;
        this.notBefore = notBefore;
        this.resource = resource;
        this.accessToken = accessToken;
    }
}
