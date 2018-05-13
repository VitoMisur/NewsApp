package com.example.vitomisur.cnewsr;

public class News {
    private String nSection;
    private String nTitle;
    private String nTime;
    private String nUrl;
    private String nContributor;

    public News(String contributor, String title, String section, String time, String url) {
        nContributor = contributor;
        nTitle = title;
        nSection = section;
        nTime = time;
        nUrl = url;
    }

    public String getContributor() {
        return nContributor;
    }

    public String getSection() {
        return nSection;
    }

    public String getTitle() {
        return nTitle;
    }

    public String getUrl() {
        return nUrl;
    }

    public String getTime() {
        return nTime;
    }
}