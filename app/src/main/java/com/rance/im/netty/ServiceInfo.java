package com.rance.im.netty;

import java.io.Serializable;

public class ServiceInfo implements Serializable {

    private String url;
    private int serverPort;
    private int httpPort;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getServerPort() {
        return serverPort;
    }


    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

}
