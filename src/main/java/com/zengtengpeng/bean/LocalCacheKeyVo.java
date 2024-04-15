package com.zengtengpeng.bean;

public class LocalCacheKeyVo {
    public LocalCacheKeyVo() {
    }

    public LocalCacheKeyVo(Integer type, String localCacheKey, String mapKey) {
        this.type = type;
        this.localCacheKey = localCacheKey;
        this.mapKey = mapKey;
    }

    /**
     * 1:object 2:map
     */
    public Integer type;

    private String localCacheKey;

    private String mapKey;


    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getLocalCacheKey() {
        return localCacheKey;
    }

    public void setLocalCacheKey(String localCacheKey) {
        this.localCacheKey = localCacheKey;
    }

    public String getMapKey() {
        return mapKey;
    }

    public void setMapKey(String mapKey) {
        this.mapKey = mapKey;
    }

    @Override
    public String toString() {
        return "LocalCacheKeyVo{" +
                "type=" + type +
                ", localCacheKey='" + localCacheKey + '\'' +
                ", mapKey='" + mapKey + '\'' +
                '}';
    }
}
