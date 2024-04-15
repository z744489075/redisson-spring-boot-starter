package com.zengtengpeng.bean;

public class CaffeineTimeBeanVo {


    //(多个实例)本地缓存缓存多久 5分钟
    private Integer localCacheMultiTime=1000*60*5;
    //(多个实例)最小的缓存条目
    private Integer localInitMultiSize=50;
    //(多个实例)最大的缓存条目
    private Integer localMaxMultiSize=1000;

    public Integer getLocalCacheMultiTime() {
        return localCacheMultiTime;
    }

    public void setLocalCacheMultiTime(Integer localCacheMultiTime) {
        this.localCacheMultiTime = localCacheMultiTime;
    }

    public Integer getLocalInitMultiSize() {
        return localInitMultiSize;
    }

    public void setLocalInitMultiSize(Integer localInitMultiSize) {
        this.localInitMultiSize = localInitMultiSize;
    }

    public Integer getLocalMaxMultiSize() {
        return localMaxMultiSize;
    }

    public void setLocalMaxMultiSize(Integer localMaxMultiSize) {
        this.localMaxMultiSize = localMaxMultiSize;
    }
}
