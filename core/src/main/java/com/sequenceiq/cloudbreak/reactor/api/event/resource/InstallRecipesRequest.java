package com.sequenceiq.cloudbreak.reactor.api.event.resource;

public class InstallRecipesRequest extends AbstractClusterUpscaleRequest {

    public InstallRecipesRequest(Long stackId, String hostGroupName) {
        super(stackId, hostGroupName);
    }
}
