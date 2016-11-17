package com.acme.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiInfo {
    private String authorizationEndpoint;

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }
}