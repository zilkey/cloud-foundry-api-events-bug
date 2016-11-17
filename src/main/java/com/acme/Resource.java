package com.acme;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class Resource<T> {
    private HashMap<String, String> metadata;
    private T entity;

    @JsonProperty("metadata")
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("entity")
    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}