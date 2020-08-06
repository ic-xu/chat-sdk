package com.rance.im.message;


public class PMessage {

    private String title;

    private String content;

    private long time;

    private String logo;


    public long getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public String getLogo() {
        return logo;
    }

    public String getTitle() {
        return title;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}