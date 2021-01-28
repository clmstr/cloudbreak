package com.sequenceiq.freeipa.client;

import java.net.URL;
import java.util.Map;

import javax.ws.rs.client.Client;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.RpcListener;

@Component
public class FreeIpaHealthCheckClientFactory extends FreeIpaClientFactory<FreeIpaHealthCheckClient> {

    protected static final String DEFAULT_BASE_PATH = "/freeipahealthcheck";

    @Value("${freeipa.healthcheck.connectionTimeoutMs}")
    private int connectionTimeoutMillis;

    @Value("${freeipa.healthcheck.readTimeoutMs}")
    private int readTimeoutMillis;

    @NotNull
    @Override
    protected FreeIpaHealthCheckClient instantiateClient(Map<String, String>  headers, RpcListener listener, Client restClient,
            URL freeIpaHealthCheckUrl) {
        return new FreeIpaHealthCheckClient(restClient, freeIpaHealthCheckUrl, headers, listener);
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
        return connectionTimeoutMillis;
    }
}
