package com.workerai.utils;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;

public class DebugAuth {
    private final String email;
    private final String password;

    private String username;
    private String accessToken;
    private String uuid;


    public DebugAuth(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public void connect() throws MicrosoftAuthenticationException {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        MicrosoftAuthResult result = authenticator.loginWithCredentials(email, password);

        username = result.getProfile().getName();
        uuid = result.getProfile().getId();
        accessToken = result.getAccessToken();

    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUuid() {
        return uuid;
    }
}
