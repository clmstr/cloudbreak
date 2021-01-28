package com.sequenceiq.node.health.client.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicesDetails {

    private Map<String, NodeHealth> freeipaServices;

    private Map<String, NodeHealth> infraServices;

    public Map<String, NodeHealth> getFreeipaServices() {
        return freeipaServices;
    }

    public void setFreeipaServices(Map<String, NodeHealth> freeipaServices) {
        this.freeipaServices = freeipaServices;
    }

    public Map<String, NodeHealth> getInfraServices() {
        return infraServices;
    }

    public void setInfraServices(Map<String, NodeHealth> infraServices) {
        this.infraServices = infraServices;
    }

    @Override
    public String toString() {
        return "ServicesDetails{" +
                "freeipaServices=" + freeipaServices +
                ", infraServices=" + infraServices +
                '}';
    }
}
