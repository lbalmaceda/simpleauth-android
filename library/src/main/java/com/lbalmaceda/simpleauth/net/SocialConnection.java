package com.lbalmaceda.simpleauth.net;

/**
 * Created by lbalmaceda on 12/12/15.
 */
public enum SocialConnection {
    FACEBOOK("facebook"),
    TWITTER("twitter");

    private String mConnection;

    SocialConnection(String connection) {
        mConnection = connection;
    }

    public String toString() {
        return mConnection;
    }
}
