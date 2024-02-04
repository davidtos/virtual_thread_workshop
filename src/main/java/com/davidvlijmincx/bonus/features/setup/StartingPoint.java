package com.davidvlijmincx.bonus.features.setup;

public class StartingPoint {

    private String url;
    private int urlsOnPage;

    public StartingPoint(String url, int urlsOnPage) {
        this.url = url;
        this.urlsOnPage = urlsOnPage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUrlsOnPage() {
        return urlsOnPage;
    }

    public void setUrlsOnPage(int urlsOnPage) {
        this.urlsOnPage = urlsOnPage;
    }
}
