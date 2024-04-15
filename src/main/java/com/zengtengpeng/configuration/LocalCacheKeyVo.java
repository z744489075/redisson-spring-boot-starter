package com.zengtengpeng.configuration;

public class LocalCacheKeyVo {
    public LocalCacheKeyVo() {
    }

    public LocalCacheKeyVo(String name) {
        this.name = name;
    }

    public LocalCacheKeyVo(String name, String localCacheKey) {
        this.name = name;
        this.localCacheKey = localCacheKey;
    }

    private String name;

    private String localCacheKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalCacheKey() {
        return localCacheKey;
    }

    public void setLocalCacheKey(String localCacheKey) {
        this.localCacheKey = localCacheKey;
    }

    @Override
    public String toString() {
        return "LocalCacheKeyVo{" +
                "name='" + name + '\'' +
                ", localCacheKey='" + localCacheKey + '\'' +
                '}';
    }
}
