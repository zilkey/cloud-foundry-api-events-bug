package com.acme;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaginatedResource<T> {
    private String nextUrl;
    private List<Resource<T>> resources;

    @JsonProperty("next_url")
    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public List<Resource<T>> getResources() {
        return resources;
    }

    public void setResources(List<Resource<T>> resources) {
        this.resources = resources;
    }
}
