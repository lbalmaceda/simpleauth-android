package com.lbalmaceda.simpleauth.net;

/**
 * Created by lbalmaceda on 12/13/15.
 */
public class EPLoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final String idToken;

    public EPLoginResponse(String accessToken, String tokenType, String idToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.idToken = idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getIdToken() {
        return idToken;
    }
}
