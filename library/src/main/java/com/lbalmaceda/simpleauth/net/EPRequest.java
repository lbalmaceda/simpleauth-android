package com.lbalmaceda.simpleauth.net;

/**
 * Created by lbalmaceda on 12/13/15.
 */
public class EPRequest {
    private final String clientId;
    private final String username;
    private final String password;
    private final String connection;
    private final String grantType;
    private final String scope;

    public EPRequest(String clientId, String username, String password) {
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.connection = "Username-Password-Authentication";
        this.grantType = "password";
        this.scope = "openid";
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConnection() {
        return connection;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getScope() {
        return scope;
    }
}
