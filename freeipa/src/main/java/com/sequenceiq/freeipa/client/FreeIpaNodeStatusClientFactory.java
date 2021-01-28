package com.sequenceiq.freeipa.client;

import java.net.URL;
import java.util.Map;

import javax.ws.rs.client.Client;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.RpcListener;
import com.sequenceiq.node.health.client.CdpNodeStatusMonitorClient;

@Component
public class FreeIpaNodeStatusClientFactory extends FreeIpaClientFactory<CdpNodeStatusMonitorClient> {

    protected static final String DEFAULT_BASE_PATH = "/nodestatus";

    @Value("${freeipa.nodestatus.connectionTimeoutMs}")
    private int connetionTimeoutMillis;

    @Value("${freeipa.nodestatus.readTimeoutMs}")
    private int readTimeoutMillis;

    @NotNull
    @Override
    protected CdpNodeStatusMonitorClient instantiateClient(Map headers, RpcListener listener, Client restClient, URL freeIpaHealthCheckUrl) {
        return new CdpNodeStatusMonitorClient(restClient, freeIpaHealthCheckUrl, headers, listener);
    }

    @NotNull
    @Override
    protected String getDefaultBasePath() {
        return DEFAULT_BASE_PATH;
    }

    @Override
    protected int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    @Override
    protected int getConnectionTimeoutMillis() {
        return connetionTimeoutMillis;
    }
}
