package com.sequenceiq.cloudbreak.cm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cloudera.api.swagger.ClouderaManagerResourceApi;
import com.cloudera.api.swagger.ClustersResourceApi;
import com.cloudera.api.swagger.CommandsResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiCommand;
import com.cloudera.api.swagger.model.ApiHostRef;
import com.cloudera.api.swagger.model.ApiHostRefList;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.InstanceStatus;
import com.sequenceiq.cloudbreak.cm.client.retry.ClouderaManagerApiFactory;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerPollingServiceProvider;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.host.HostGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.polling.PollingResult;

@ExtendWith(MockitoExtension.class)
class ClouderaManagerDecomissionerTest {

    private static final String STACK_NAME = "stack";

    private static final String DELETED_INSTANCE_FQDN = "deletedInstance";

    private static final String RUNNING_INSTANCE_FQDN = "runningInstance";

    @InjectMocks
    private ClouderaManagerDecomissioner underTest;

    @Mock
    private ClouderaManagerApiFactory clouderaManagerApiFactory;

    @Mock
    private ApiClient client;

    @Mock
    private ClustersResourceApi clustersResourceApi;

    @Mock
    private ClouderaManagerResourceApi clouderaManagerResourceApi;

    @Mock
    private CommandsResourceApi commandsResourceApi;

    @Mock
    private ClouderaManagerPollingServiceProvider pollingServiceProvider;

    @Test
    public void testDecommissionForLostNodesIfFirstDecommissionSucceeded() throws ApiException {
        mockListHosts();
        mockDecommission(Pair.of(BigDecimal.ONE, PollingResult.SUCCESS));
        mockAbortCommand(BigDecimal.ONE);
        InstanceMetaData deletedInstanceMetadata = createDeletedInstanceMetadata();

        underTest.decommissionNodes(getStack(), Map.of(deletedInstanceMetadata.getDiscoveryFQDN(), deletedInstanceMetadata), client);

        verifyNoInteractions(commandsResourceApi);
        verify(clouderaManagerResourceApi, times(1)).hostsDecommissionCommand(any());
    }

    @Test
    public void testDecommissionForLostNodesIfSecondDecommissionSucceeded() throws ApiException {
        mockListHosts();
        mockDecommission(Pair.of(BigDecimal.ONE, PollingResult.TIMEOUT),
                Pair.of(BigDecimal.TEN, PollingResult.SUCCESS));
        mockAbortCommand(BigDecimal.ONE);
        InstanceMetaData deletedInstanceMetadata = createDeletedInstanceMetadata();

        underTest.decommissionNodes(getStack(), Map.of(deletedInstanceMetadata.getDiscoveryFQDN(), deletedInstanceMetadata), client);

        verify(commandsResourceApi, times(1)).abortCommand(eq(BigDecimal.ONE));
        verify(clouderaManagerResourceApi, times(2)).hostsDecommissionCommand(any());
    }

    @Test
    public void testDecommissionForLostNodesIfBothDecommissionFails() throws ApiException {
        mockListHosts();
        mockDecommission(Pair.of(BigDecimal.ONE, PollingResult.TIMEOUT),
                Pair.of(BigDecimal.TEN, PollingResult.TIMEOUT));
        mockAbortCommand(BigDecimal.ONE, BigDecimal.TEN);
        InstanceMetaData deletedInstanceMetadata = createDeletedInstanceMetadata();

        underTest.decommissionNodes(getStack(), Map.of(deletedInstanceMetadata.getDiscoveryFQDN(), deletedInstanceMetadata), client);

        verify(commandsResourceApi, times(1)).abortCommand(eq(BigDecimal.ONE));
        verify(commandsResourceApi, times(1)).abortCommand(eq(BigDecimal.TEN));
        verify(clouderaManagerResourceApi, times(2)).hostsDecommissionCommand(any());
    }

    @Test
    public void collectHostsToRemoveShouldCollectDeletedOnProviderSideNodes() throws Exception {
        mockListHosts();
        Set<String> hostNames = Set.of(DELETED_INSTANCE_FQDN, RUNNING_INSTANCE_FQDN);

        Map<String, InstanceMetaData> result = underTest.collectHostsToRemove(getStack(), createHostGroup(), hostNames, client);

        assertThat(result.keySet())
                .contains(createDeletedInstanceMetadata().getDiscoveryFQDN(),
                        createRunningInstanceMetadata().getDiscoveryFQDN());
    }

    private HostGroup createHostGroup() {
        HostGroup hostGroup = new HostGroup();
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setInstanceMetaData(Set.of(createDeletedInstanceMetadata(), createRunningInstanceMetadata()));
        hostGroup.setInstanceGroup(instanceGroup);
        return hostGroup;
    }

    private void mockDecommission(Pair<BigDecimal, PollingResult> resultPair, Pair<BigDecimal, PollingResult>... resultPairs)
            throws ApiException {
        when(clouderaManagerApiFactory.getClouderaManagerResourceApi(eq(client))).thenReturn(clouderaManagerResourceApi);
        when(clouderaManagerResourceApi.hostsDecommissionCommand(any())).thenReturn(getApiCommand(resultPair.getLeft()),
                Arrays.stream(resultPairs).map(resultPairItem -> getApiCommand(resultPairItem.getLeft())).toArray(ApiCommand[]::new));
        when(pollingServiceProvider.startPollingCmHostDecommissioning(any(), eq(client), any(), anyBoolean(), anyInt())).thenReturn(resultPair.getRight(),
                Arrays.stream(resultPairs).map(resultPairItem -> resultPairItem.getRight()).toArray(PollingResult[]::new));
    }

    private void mockAbortCommand(BigDecimal commandId, BigDecimal... commandIds) throws ApiException {
        lenient().when(clouderaManagerApiFactory.getCommandsResourceApi(eq(client))).thenReturn(commandsResourceApi);
        lenient().when(commandsResourceApi.abortCommand(any())).thenReturn(getApiCommand(commandId),
                Arrays.stream(commandIds).map(commandIdItem -> getApiCommand(commandIdItem)).toArray(ApiCommand[]::new));
    }

    private void mockListHosts() throws ApiException {
        ApiHostRefList apiHostRefList = new ApiHostRefList();
        apiHostRefList.setItems(List.of(createApiHostRef(DELETED_INSTANCE_FQDN), createApiHostRef(RUNNING_INSTANCE_FQDN)));
        when(clustersResourceApi.listHosts(STACK_NAME, null, null)).thenReturn(apiHostRefList);
        when(clouderaManagerApiFactory.getClustersResourceApi(client)).thenReturn(clustersResourceApi);
    }

    private InstanceMetaData createRunningInstanceMetadata() {
        return createInstanceMetadata(InstanceStatus.SERVICES_HEALTHY, RUNNING_INSTANCE_FQDN);
    }

    private InstanceMetaData createDeletedInstanceMetadata() {
        return createInstanceMetadata(InstanceStatus.DELETED_ON_PROVIDER_SIDE, DELETED_INSTANCE_FQDN);
    }

    private InstanceMetaData createInstanceMetadata(InstanceStatus servicesHealthy, String runningInstanceFqdn) {
        InstanceMetaData runningInstanceMetaData = new InstanceMetaData();
        runningInstanceMetaData.setInstanceStatus(servicesHealthy);
        runningInstanceMetaData.setDiscoveryFQDN(runningInstanceFqdn);
        return runningInstanceMetaData;
    }

    private ApiHostRef createApiHostRef(String deletedInstanceFqd) {
        ApiHostRef deletedInstanceHostRef = new ApiHostRef();
        deletedInstanceHostRef.setHostname(deletedInstanceFqd);
        return deletedInstanceHostRef;
    }

    private ApiCommand getApiCommand(BigDecimal commandId) {
        ApiCommand apiCommand = new ApiCommand();
        apiCommand.setId(commandId);
        return apiCommand;
    }

    private Stack getStack() {
        Stack stack = new Stack();
        stack.setName(STACK_NAME);
        return stack;
    }
}
