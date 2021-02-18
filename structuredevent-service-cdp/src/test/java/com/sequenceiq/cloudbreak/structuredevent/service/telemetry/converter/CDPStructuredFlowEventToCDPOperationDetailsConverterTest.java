package com.sequenceiq.cloudbreak.structuredevent.service.telemetry.converter;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import com.cloudera.thunderhead.service.common.usage.UsageProto;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.structuredevent.event.FlowDetails;
import com.sequenceiq.cloudbreak.structuredevent.event.cdp.CDPOperationDetails;
import com.sequenceiq.cloudbreak.structuredevent.event.cdp.environment.CDPEnvironmentStructuredFlowEvent;
import com.sequenceiq.cloudbreak.structuredevent.service.telemetry.mapper.EnvironmentRequestProcessingStepMapper;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;

class CDPStructuredFlowEventToCDPOperationDetailsConverterTest {

    private CDPStructuredFlowEventToCDPOperationDetailsConverter underTest;

    @BeforeEach()
    public void setUp() {
        underTest = new CDPStructuredFlowEventToCDPOperationDetailsConverter();
        Whitebox.setInternalState(underTest, "appVersion", "version-1234");
        Whitebox.setInternalState(underTest, "environmentRequestProcessingStepMapper", new EnvironmentRequestProcessingStepMapper());
    }

    @Test
    public void testConvertWithNull() {
        Assert.assertNull("We should return with null if the input is null", underTest.convert(null));
    }

    @Test
    public void testConversionWithNullOperation() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals("", details.getAccountId());
        Assert.assertEquals("", details.getResourceCrn());
        Assert.assertEquals("", details.getResourceName());
        Assert.assertEquals("", details.getInitiatorCrn());
        Assert.assertEquals("", details.getCorrelationId());
        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.UNSET, details.getCdpRequestProcessingStep());
        Assert.assertEquals("", details.getFlowId());
        Assert.assertEquals("", details.getFlowChainId());
        Assert.assertEquals("", details.getFlowState());
        Assert.assertEquals(UsageProto.CDPEnvironmentsEnvironmentType.Value.UNSET, details.getEnvironmentType());

        Assert.assertEquals("version-1234", details.getApplicationVersion());
    }

    @Test
    public void testInitProcessingType() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        FlowDetails flowDetails = new FlowDetails();
        flowDetails.setFlowState("unknown");
        flowDetails.setNextFlowState("INIT_STATE");
        cdpStructuredFlowEvent.setFlow(flowDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.INIT, details.getCdpRequestProcessingStep());
        Assert.assertEquals("", details.getFlowState());
    }

    @Test
    public void testFinalProcessingType() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        FlowDetails flowDetails = new FlowDetails();
        flowDetails.setNextFlowState("FINAL_STATE");
        cdpStructuredFlowEvent.setFlow(flowDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.FINAL, details.getCdpRequestProcessingStep());
        Assert.assertEquals("", details.getFlowState());
    }

    @Test
    public void testSomethingElseProcessingType() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        FlowDetails flowDetails = new FlowDetails();
        flowDetails.setNextFlowState("SOMETHING_ELSE");
        cdpStructuredFlowEvent.setFlow(flowDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.UNSET, details.getCdpRequestProcessingStep());
    }

    @Test
    public void testFlowAndFlowChainType() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        FlowDetails flowDetails = new FlowDetails();
        flowDetails.setFlowId("flowId");
        flowDetails.setFlowChainId("flowChainId");
        flowDetails.setNextFlowState("FINAL_STATE");
        cdpStructuredFlowEvent.setFlow(flowDetails);

        CDPOperationDetails operationDetails = new CDPOperationDetails();
        operationDetails.setUuid("correlationId");
        cdpStructuredFlowEvent.setOperation(operationDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.FINAL, details.getCdpRequestProcessingStep());
        Assert.assertEquals("flowId", details.getFlowId());
        Assert.assertEquals("flowChainId", details.getFlowChainId());
        Assert.assertEquals("correlationId", details.getCorrelationId());
    }

    @Test
    public void testNoFlowChain() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        FlowDetails flowDetails = new FlowDetails();
        flowDetails.setFlowId("flowId");
        flowDetails.setFlowState("SOMETHING");
        flowDetails.setNextFlowState("FINAL_STATE");
        cdpStructuredFlowEvent.setFlow(flowDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.FINAL, details.getCdpRequestProcessingStep());
        Assert.assertEquals("flowId", details.getFlowId());
        Assert.assertEquals("flowId", details.getFlowChainId());
        Assert.assertEquals("SOMETHING", details.getFlowState());
    }

    @Test
    public void testEnvironmentTypeSetCorrectly() {
        CDPEnvironmentStructuredFlowEvent cdpStructuredFlowEvent = new CDPEnvironmentStructuredFlowEvent();
        EnvironmentDto environmentDetails = new EnvironmentDto();
        environmentDetails.setCloudPlatform(CloudPlatform.AWS.name());
        cdpStructuredFlowEvent.setPayload(environmentDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(cdpStructuredFlowEvent);

        Assert.assertEquals(UsageProto.CDPEnvironmentsEnvironmentType.Value.AWS, details.getEnvironmentType());    }
}