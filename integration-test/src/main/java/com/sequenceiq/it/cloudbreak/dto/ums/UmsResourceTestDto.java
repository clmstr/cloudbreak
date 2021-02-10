package com.sequenceiq.it.cloudbreak.dto.ums;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.emptyRunningParameter;

import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.action.ums.AssignResourceRequest;
import com.sequenceiq.it.cloudbreak.assign.Assignable;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.AbstractTestDto;

@Prototype
public class UmsResourceTestDto extends AbstractTestDto<AssignResourceRequest, Object, UmsResourceTestDto, UmsClient> {

    private static final String UMS = "UMS";

    private static final String DH_CREATOR_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:DataHubCreator";

    private static final String DH_ADMIN_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:DatahubAdmin";

    private static final String DH_USER_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:DatahubUser";

    private static final String ENV_USER_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:EnvironmentUser";

    private static final String ENV_ADMIN_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:EnvironmentAdmin";

    private static final String DATA_STEWARD_CRN = "crn:altus:iam:us-west-1:altus:resourceRole:DataSteward";

    public UmsResourceTestDto(TestContext testContext) {
        super(new AssignResourceRequest(), testContext);
    }

    public UmsResourceTestDto() {
        super(UmsResourceTestDto.class.getSimpleName().toUpperCase());
        setRequest(new AssignResourceRequest());
    }

    public UmsResourceTestDto withDatahubCreator() {
        getRequest().setRoleCrn(DH_CREATOR_CRN);
        return this;
    }

    public UmsResourceTestDto withEnvironmentUser() {
        getRequest().setRoleCrn(ENV_USER_CRN);
        return this;
    }

    public UmsResourceTestDto withEnvironmentAdmin() {
        getRequest().setRoleCrn(ENV_ADMIN_CRN);
        return this;
    }

    public UmsResourceTestDto withDataSteward() {
        getRequest().setRoleCrn(DATA_STEWARD_CRN);
        return this;
    }

    public UmsResourceTestDto withDatahubAdmin() {
        getRequest().setRoleCrn(DH_USER_CRN);
        return this;
    }

    public UmsResourceTestDto withDatahubUser() {
        getRequest().setRoleCrn(DH_USER_CRN);
        return this;
    }

    public UmsResourceTestDto assignTarget(String key) {
        try {
            Assignable dto = getTestContext().get(key);
            getRequest().setResourceCrn(dto.getCrn());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("TestContext member with key %s does not implement %s interface",
                    key, Assignable.class.getCanonicalName()), e);
        }
        return this;
    }

    public UmsResourceTestDto valid() {
        return new UmsResourceTestDto();
    }

    @Override
    public UmsResourceTestDto when(Action<UmsResourceTestDto, UmsClient> action) {
        return getTestContext().when((UmsResourceTestDto) this, UmsClient.class, action, emptyRunningParameter());
    }
}