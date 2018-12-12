package com.example.bean;

import java.io.Serializable;

public abstract class Base implements Serializable {
    private String cacheKey;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

}
