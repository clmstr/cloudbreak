package com.sequenceiq.it.cloudbreak.testcase.mock;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sequenceiq.freeipa.api.v1.kerberos.model.create.ActiveDirectoryKerberosDescriptor;
import com.sequenceiq.freeipa.api.v1.kerberos.model.create.CreateKerberosConfigRequest;
import com.sequenceiq.freeipa.api.v1.kerberos.model.create.FreeIpaKerberosDescriptor;
import com.sequenceiq.freeipa.api.v1.kerberos.model.create.MITKerberosDescriptor;
import com.sequenceiq.it.cloudbreak.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.assertion.Assertion;
import com.sequenceiq.it.cloudbreak.assertion.MockVerification;
import com.sequenceiq.it.cloudbreak.client.KerberosTestClient;
import com.sequenceiq.it.cloudbreak.client.StackTestClient;
import com.sequenceiq.it.cloudbreak.cloud.HostGroupType;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.MockedTestContext;
import com.sequenceiq.it.cloudbreak.context.TestCaseDescription;
import com.sequenceiq.it.cloudbreak.dto.ClusterTestDto;
import com.sequenceiq.it.cloudbreak.dto.InstanceGroupTestDto;
import com.sequenceiq.it.cloudbreak.dto.stack.StackTestDto;

public class KerberosConfigTest extends AbstractMockTest {

    private static final String SALT_HIGHSTATE = "state.highstate";

    @Inject
    private KerberosTestClient kerberosTestClient;

    @Inject
    private StackTestClient stackTestClient;

    @Test(dataProvider = "dataProviderForTest")
    public void testClusterCreationWithValidKerberos(MockedTestContext testContext, String blueprintName, KerberosTestData testData,
            @Description TestCaseDescription testCaseDescription) {
        CreateKerberosConfigRequest request = testData.getRequest();
        request.setName(extendNameWithGeneratedPart(request.getName()));
        testContext
                .given("master", InstanceGroupTestDto.class)
                .withHostGroup(HostGroupType.MASTER)
                .withNodeCount(1)
                .given(ClusterTestDto.class)
                .given(StackTestDto.class)
                .withInstanceGroupsEntity(InstanceGroupTestDto.defaultHostGroup(testContext))
                .when(stackTestClient.createV4())
                .await(STACK_AVAILABLE)
                .then(testData.getAssertions())
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a cluster setup without kerberosname",
            when = "calling cluster creation",
            then = "the cluster should not been kerberized")
    public void testClusterCreationAttemptWithKerberosConfigWithoutName(MockedTestContext testContext) {
        testContext
                .given("master", InstanceGroupTestDto.class)
                .withHostGroup(HostGroupType.MASTER)
                .withNodeCount(1)
                .given(ClusterTestDto.class)
                .given(StackTestDto.class)
                .withInstanceGroupsEntity(InstanceGroupTestDto.defaultHostGroup(testContext))
                .when(stackTestClient.createV4())
                .await(STACK_AVAILABLE)
                .validate();
    }

    @DataProvider(name = "dataProviderForTest")
    public Object[][] provide() {
        return new Object[][]{
                {
                        getBean(MockedTestContext.class),
                        resourcePropertyProvider().getName(),
                        KerberosTestData.FREEIPA,
                        new TestCaseDescription.TestCaseDescriptionBuilder()
                                .given("a valid stack request and a FreeIPA based kerberos configuration")
                                .when("calling create kerberos configuration and a cluster creation with that kerberos configuration")
                                .then("the cluster should be available")
                },
                {
                        getBean(MockedTestContext.class),
                        resourcePropertyProvider().getName(),
                        KerberosTestData.ACTIVE_DIRECTORY,
                        new TestCaseDescription.TestCaseDescriptionBuilder()
                                .given("a valid stack request and a Active Directory based kerberos configuration")
                                .when("calling create kerberos configuration and a cluster creation with that kerberos configuration")
                                .then("the cluster should be available")
                },
                {
                        getBean(MockedTestContext.class),
                        resourcePropertyProvider().getName(),
                        KerberosTestData.MIT,
                        new TestCaseDescription.TestCaseDescriptionBuilder()
                                .given("a valid stack request and a MIT based kerberos configuration")
                                .when("calling create kerberos configuration and a cluster creation with that kerberos configuration")
                                .then("the cluster should be available")
                }
        };
    }

    private String extendNameWithGeneratedPart(String name) {
        return String.format("%s-%s", name, resourcePropertyProvider().getName());
    }

    private enum KerberosTestData {

        ACTIVE_DIRECTORY {
            @Override
            public List<Assertion<StackTestDto, CloudbreakClient>> getAssertions() {
                List<Assertion<StackTestDto, CloudbreakClient>> verifications = new LinkedList<>();
                verifications.add(clusterTemplatePostToCMContains("enableKerberos").exactTimes(1));
                verifications.add(MockVerification.verify(HttpMethod.POST, "SaltMock.SALT_RUN").bodyContains(SALT_HIGHSTATE).exactTimes(2));
                return verifications;
            }

            @Override
            public CreateKerberosConfigRequest getRequest() {
                CreateKerberosConfigRequest request = new CreateKerberosConfigRequest();
                request.setName("adKerberos");
                ActiveDirectoryKerberosDescriptor activeDirectory = new ActiveDirectoryKerberosDescriptor();
                activeDirectory.setTcpAllowed(true);
                activeDirectory.setPrincipal("admin/principal");
                activeDirectory.setPassword("kerberosPassword");
                activeDirectory.setUrl("someurl.com");
                activeDirectory.setAdminUrl("admin.url.com");
                activeDirectory.setRealm("realm");
                activeDirectory.setLdapUrl("otherurl.com");
                activeDirectory.setContainerDn("{}");
                request.setActiveDirectory(activeDirectory);
                return request;
            }
        },

        MIT {
            @Override
            public List<Assertion<StackTestDto, CloudbreakClient>> getAssertions() {
                List<Assertion<StackTestDto, CloudbreakClient>> verifications = new LinkedList<>();
                verifications.add(clusterTemplatePostToCMContains("enableKerberos").exactTimes(1));
                verifications.add(MockVerification.verify(HttpMethod.POST, "SaltMock.SALT_RUN").bodyContains(SALT_HIGHSTATE).exactTimes(2));
                return verifications;
            }

            @Override
            public CreateKerberosConfigRequest getRequest() {
                CreateKerberosConfigRequest request = new CreateKerberosConfigRequest();
                request.setName("mitKerberos");
                MITKerberosDescriptor mit = new MITKerberosDescriptor();
                mit.setTcpAllowed(true);
                mit.setPrincipal("kerberosPrincipal");
                mit.setPassword("kerberosPassword");
                mit.setUrl("kerberosproviderurl.com");
                mit.setAdminUrl("kerberosadminurl.com");
                mit.setRealm("kerbRealm");
                request.setMit(mit);
                return request;
            }
        },

        FREEIPA {
            @Override
            public List<Assertion<StackTestDto, CloudbreakClient>> getAssertions() {
                List<Assertion<StackTestDto, CloudbreakClient>> verifications = new LinkedList<>();
                verifications.add(clusterTemplatePostToCMContains("enableKerberos").exactTimes(1));
                verifications.add(MockVerification.verify(HttpMethod.POST, "SaltMock.SALT_RUN").bodyContains(SALT_HIGHSTATE).exactTimes(2));
                return verifications;
            }

            @Override
            public CreateKerberosConfigRequest getRequest() {
                CreateKerberosConfigRequest request = new CreateKerberosConfigRequest();
                FreeIpaKerberosDescriptor freeIpaRequest = new FreeIpaKerberosDescriptor();
                freeIpaRequest.setAdminUrl("http://someurl.com");
                freeIpaRequest.setRealm("someRealm");
                freeIpaRequest.setUrl("http://someadminurl.com");
                freeIpaRequest.setRealm("realm");
                freeIpaRequest.setPassword("freeipapassword");
                freeIpaRequest.setPrincipal("kerberosPrincipal");
                request.setName("freeIpaTest");
                request.setDescription("some free ipa description");
                request.setFreeIpa(freeIpaRequest);
                return request;
            }
        };

        public abstract List<Assertion<StackTestDto, CloudbreakClient>> getAssertions();

        public abstract CreateKerberosConfigRequest getRequest();

        private static MockVerification clusterTemplatePostToCMContains(String content) {
            return MockVerification.verify(HttpMethod.POST, "ClouderaManagerMock.IMPORT_CLUSTERTEMPLATE").bodyContains(content);
        }

    }
}
