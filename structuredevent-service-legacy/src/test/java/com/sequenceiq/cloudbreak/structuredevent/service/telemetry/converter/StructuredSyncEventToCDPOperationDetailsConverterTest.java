package com.sequenceiq.cloudbreak.structuredevent.service.telemetry.converter;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import com.cloudera.thunderhead.service.common.usage.UsageProto;
import com.sequenceiq.cloudbreak.structuredevent.event.StructuredSyncEvent;
import com.sequenceiq.cloudbreak.structuredevent.event.legacy.OperationDetails;

public class StructuredSyncEventToCDPOperationDetailsConverterTest {

    private StructuredSyncEventToCDPOperationDetailsConverter underTest;

    @BeforeEach
    public void setUp() {
        underTest = new StructuredSyncEventToCDPOperationDetailsConverter();
        Whitebox.setInternalState(underTest, "appVersion", "version-1234");
    }

    @Test
    public void testConvertWithNull() {
        Assert.assertNull("We should return with null if the input is null", underTest.convert(null));
    }

    @Test
    public void testConversionWithNullOperation() {
        StructuredSyncEvent structuredSyncEvent = new StructuredSyncEvent();

        UsageProto.CDPOperationDetails details = underTest.convert(structuredSyncEvent);

        Assert.assertEquals("", details.getAccountId());
        Assert.assertEquals("", details.getResourceCrn());
        Assert.assertEquals("", details.getResourceName());
        Assert.assertEquals("", details.getInitiatorCrn());

        Assert.assertEquals("version-1234", details.getApplicationVersion());
        Assert.assertEquals(UsageProto.CDPRequestProcessingStep.Value.UNSET, details.getCdpRequestProcessingStep());
    }

    @Test
    public void testFlowRelatedOperationDetailsFieldsReturnEmptyString() {
        StructuredSyncEvent structuredSyncEvent = new StructuredSyncEvent();
        OperationDetails operationDetails = new OperationDetails();
        operationDetails.setTenant("tenant1");
        operationDetails.setResourceCrn("crn1");
        operationDetails.setResourceName("name1");
        operationDetails.setUserCrn("crn2");
        structuredSyncEvent.setOperation(operationDetails);

        UsageProto.CDPOperationDetails details = underTest.convert(structuredSyncEvent);

        Assert.assertEquals("tenant1", details.getAccountId());
        Assert.assertEquals("crn1", details.getResourceCrn());
        Assert.assertEquals("name1", details.getResourceName());
        Assert.assertEquals("crn2", details.getInitiatorCrn());

        Assert.assertEquals("", details.getFlowId());
        Assert.assertEquals("", details.getFlowChainId());
        Assert.assertEquals("", details.getFlowState());
    }
}
