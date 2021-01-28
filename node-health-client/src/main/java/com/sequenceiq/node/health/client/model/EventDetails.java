package com.sequenceiq.node.health.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDetails {

    private String meteredresourcecrn;

    private String meteredresourcename;

    private String servicetype;

    public String getMeteredresourcecrn() {
        return meteredresourcecrn;
    }

    public void setMeteredresourcecrn(String meteredresourcecrn) {
        this.meteredresourcecrn = meteredresourcecrn;
    }

    public String getMeteredresourcename() {
        return meteredresourcename;
    }

    public void setMeteredresourcename(String meteredresourcename) {
        this.meteredresourcename = meteredresourcename;
    }

    public String getServicetype() {
        return servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }

    @Override
    public String toString() {
        return "EventDetails{" +
                "meteredresourcecrn='" + meteredresourcecrn + '\'' +
                ", meteredresourcename='" + meteredresourcename + '\'' +
                ", servicetype='" + servicetype + '\'' +
                '}';
    }
}
