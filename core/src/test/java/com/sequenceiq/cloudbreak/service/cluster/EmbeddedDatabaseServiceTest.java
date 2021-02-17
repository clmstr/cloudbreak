package com.sequenceiq.cloudbreak.service.cluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.InstanceMetadataType;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.database.DatabaseAvailabilityType;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.domain.VolumeTemplate;
import com.sequenceiq.cloudbreak.domain.VolumeUsageType;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.service.stack.CloudParameterCache;
import com.sequenceiq.cloudbreak.service.template.TemplateService;
import com.sequenceiq.common.api.type.InstanceGroupType;

@ExtendWith(MockitoExtension.class)
public class EmbeddedDatabaseServiceTest {
    private static final String ACCOUNT_ID = "cloudera";

    private static final String CLOUDPLATFORM = "cloudplatform";

    @Mock
    private CloudParameterCache cloudParameterCache;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private EmbeddedDatabaseService underTest;

    @Test
    public void testIsEmbeddedDatabaseOnAttachedDiskEnabled() {
        // GIVEN
        Stack stack = createStack(1);
        when(cloudParameterCache.isVolumeAttachmentSupported(CLOUDPLATFORM)).thenReturn(true);
        // WHEN
        boolean actualResult = underTest.isEmbeddedDatabaseOnAttachedDiskEnabled(ACCOUNT_ID, stack, null);
        // THEN
        assertTrue(actualResult);
    }

    @Test
    public void testIsEmbeddedDatabaseOnAttachedDiskEnabledWhenAttachedDiskEntitlementIsEnabledButNoDisksAttachedSupported() {
        // GIVEN
        Stack stack = createStack(0);
        when(cloudParameterCache.isVolumeAttachmentSupported(CLOUDPLATFORM)).thenReturn(false);
        // WHEN
        boolean actualResult = underTest.isEmbeddedDatabaseOnAttachedDiskEnabled(ACCOUNT_ID, stack, null);
        // THEN
        assertFalse(actualResult);
    }

    @Test
    public void testIsEmbeddedDatabaseOnAttachedDiskEnabledWhenExternalDBUsed() {
        // GIVEN
        Stack stack = createStack(0);
        stack.setExternalDatabaseCreationType(DatabaseAvailabilityType.NON_HA);
        // WHEN
        boolean actualResult = underTest.isEmbeddedDatabaseOnAttachedDiskEnabled(ACCOUNT_ID, stack, null);
        // THEN
        assertFalse(actualResult);
    }

    @Test
    public void testIsEmbeddedDatabaseOnAttachedDiskEnabledWhenExternalDBCrnSet() {
        // GIVEN
        Stack stack = createStack(0);
        Cluster cluster = new Cluster();
        cluster.setDatabaseServerCrn("dbcrn");
        // WHEN
        boolean actualResult = underTest.isEmbeddedDatabaseOnAttachedDiskEnabled(ACCOUNT_ID, stack, cluster);
        // THEN
        assertFalse(actualResult);
    }

    @Test
    public void testIsAttachedDiskForEmbeddedDatabaseCreated() {
        // GIVEN
        Stack stack = createStack(1);
        Cluster cluster = new Cluster();
        cluster.setEmbeddedDatabaseOnAttachedDisk(true);
        stack.setCluster(cluster);
        when(templateService.get(any())).thenReturn(stack.getInstanceGroups().stream()
                .filter(ig -> ig.getInstanceGroupType() == InstanceGroupType.GATEWAY).findFirst().get().getTemplate());
        // WHEN
        boolean actualResult = underTest.isAttachedDiskForEmbeddedDatabaseCreated(stack);
        // THEN
        assertTrue(actualResult);
    }

    @Test
    public void testIsAttachedDiskForEmbeddedDatabaseCreatedWhenNoVolumeAttached() {
        // GIVEN
        Stack stack = createStack(0);
        when(templateService.get(any())).thenReturn(stack.getInstanceGroups().stream()
                .filter(ig -> ig.getInstanceGroupType() == InstanceGroupType.GATEWAY).findFirst().get().getTemplate());
        Cluster cluster = new Cluster();
        cluster.setEmbeddedDatabaseOnAttachedDisk(true);
        stack.setCluster(cluster);
        // WHEN
        boolean actualResult = underTest.isAttachedDiskForEmbeddedDatabaseCreated(stack);
        // THEN
        assertFalse(actualResult);
    }

    @Test
    public void testIsAttachedDiskForEmbeddedDatabaseCreatedWhenNoTemplate() {
        // GIVEN
        Stack stack = createStackWithoutTemplate();
        Cluster cluster = new Cluster();
        cluster.setEmbeddedDatabaseOnAttachedDisk(true);
        stack.setCluster(cluster);
        // WHEN
        boolean actualResult = underTest.isAttachedDiskForEmbeddedDatabaseCreated(stack);
        // THEN
        assertFalse(actualResult);
    }

    @Test
    public void testIsAttachedDiskForEmbeddedDatabaseCreatedWhenDbOnAttachedDisIsDisabled() {
        // GIVEN
        Stack stack = createStack(1);
        Cluster cluster = new Cluster();
        cluster.setEmbeddedDatabaseOnAttachedDisk(false);
        stack.setCluster(cluster);
        // WHEN
        boolean actualResult = underTest.isAttachedDiskForEmbeddedDatabaseCreated(stack);
        // THEN
        assertFalse(actualResult);
    }

    private Stack createStack(int volumeCount) {
        Stack stack = new Stack();
        InstanceGroup masterGroup = new InstanceGroup();
        masterGroup.setInstanceGroupType(InstanceGroupType.GATEWAY);
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        instanceMetaData.setInstanceGroup(masterGroup);
        instanceMetaData.setInstanceMetadataType(InstanceMetadataType.GATEWAY_PRIMARY);
        masterGroup.setInstanceMetaData(Set.of(instanceMetaData));
        Template template = new Template();
        VolumeTemplate volumeTemplate = new VolumeTemplate();
        volumeTemplate.setVolumeCount(volumeCount);
        volumeTemplate.setUsageType(VolumeUsageType.DATABASE);
        template.setVolumeTemplates(Set.of(volumeTemplate));
        masterGroup.setTemplate(template);
        stack.setInstanceGroups(Set.of(masterGroup));
        stack.setCloudPlatform(CLOUDPLATFORM);
        return stack;
    }

    private Stack createStackWithoutTemplate() {
        Stack stack = new Stack();
        InstanceGroup masterGroup = new InstanceGroup();
        masterGroup.setInstanceGroupType(InstanceGroupType.GATEWAY);
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        instanceMetaData.setInstanceGroup(masterGroup);
        instanceMetaData.setInstanceMetadataType(InstanceMetadataType.GATEWAY_PRIMARY);
        masterGroup.setInstanceMetaData(Set.of(instanceMetaData));
        stack.setInstanceGroups(Set.of(masterGroup));
        return stack;
    }
}
