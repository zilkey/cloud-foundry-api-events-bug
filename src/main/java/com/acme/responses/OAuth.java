package com.acme.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth {
    private String accessToken;

    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}