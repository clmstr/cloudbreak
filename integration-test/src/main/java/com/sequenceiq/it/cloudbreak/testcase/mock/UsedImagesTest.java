package com.sequenceiq.it.cloudbreak.testcase.mock;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.expectedMessage;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.endpoint.v4.util.responses.UsedImageStacksV4Response;
import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common.Status;
import com.sequenceiq.freeipa.api.v1.util.model.UsedImageStacksV1Response;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakActor;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakUser;
import com.sequenceiq.it.cloudbreak.client.EnvironmentTestClient;
import com.sequenceiq.it.cloudbreak.client.FreeIpaTestClient;
import com.sequenceiq.it.cloudbreak.client.ImageCatalogTestClient;
import com.sequenceiq.it.cloudbreak.client.SdxTestClient;
import com.sequenceiq.it.cloudbreak.client.UtilTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.MockedTestContext;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentNetworkTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeipaUsedImagesTestDto;
import com.sequenceiq.it.cloudbreak.dto.imagecatalog.ImageCatalogTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.util.UsedImagesTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.sdx.api.model.SdxClusterStatusResponse;

public class UsedImagesTest extends AbstractMockTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsedImagesTest.class);

    private static final String FREEIPA_IMAGE_UUID = "freeipa-image-uuid";

    private static final String SDX_IMAGE_UUID = "sdx-image-uuid";

    @Inject
    private EnvironmentTestClient environmentTestClient;

    @Inject
    private ImageCatalogTestClient imageCatalogTestClient;

    @Inject
    private SdxTestClient sdxTestClient;

    @Inject
    private UtilTestClient utilTestClient;

    @Inject
    private FreeIpaTestClient freeIpaTestClient;

    @Inject
    private CloudbreakActor cloudbreakActor;

    @Override
    protected void setupTest(TestContext testContext) {
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
        initializeDefaultBlueprints(testContext);
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a running environment with an sdx",
            when = "list used images requests are sent to cb and freeipa",
            then = "response should have the images"
    )
    public void testUsedImages(MockedTestContext testContext) {
        String cbImageCatalogName = resourcePropertyProvider().getName();
        CloudbreakUser internalActor = cloudbreakActor.createInternal(testContext.getActingUserCrn().getAccountId());

        testContext
                .given(ImageCatalogTestDto.class)
                    .withName(cbImageCatalogName)
                    .withUrl(getImageCatalogMockServerSetup().getPreWarmedImageCatalogUrlWithDefaultImageUuid(SDX_IMAGE_UUID))
                .when(imageCatalogTestClient.createV4())

                .given(EnvironmentNetworkTestDto.class)
                .given(EnvironmentTestDto.class)
                    .withNetwork()
                    .withCreateFreeIpa(Boolean.FALSE)
                .when(environmentTestClient.create())
                .await(EnvironmentStatus.AVAILABLE)
                .when(environmentTestClient.describe())

                .given(FreeIpaTestDto.class)
                    .withCatalog(getImageCatalogMockServerSetup().getFreeIpaImageCatalogUrlWitdDefaultImageUuid(FREEIPA_IMAGE_UUID))
                .when(freeIpaTestClient.create())
                .await(Status.AVAILABLE)
                .when(freeIpaTestClient.describe())

                .given(SdxInternalTestDto.class)
                    .withImageCatalogNameAndImageId(cbImageCatalogName, SDX_IMAGE_UUID)
                .when(sdxTestClient.createInternal())
                .await(SdxClusterStatusResponse.RUNNING)
                .when(sdxTestClient.describeInternal())

                .then((tc, testDto, client) -> verifyUsedImagesEndpointAuthorization(tc, testDto))
                .then((tc, testDto, client) -> verifyImagesAreUsed(tc, testDto, internalActor))

                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.delete())
                .await(EnvironmentStatus.ARCHIVED)

                .then((tc, testDto, client) -> verifyImagesAreNotUsed(tc, testDto, internalActor))
                .validate();
    }

    private SdxInternalTestDto verifyUsedImagesEndpointAuthorization(TestContext testContext, SdxInternalTestDto testDto) {
        testContext
                .as(cloudbreakActor.defaultUser())

                .given(UsedImagesTestDto.class)
                .whenException(utilTestClient.usedImages(), ForbiddenException.class, expectedMessage("You have no access to this resource."))

                .given(FreeipaUsedImagesTestDto.class)
                .whenException(freeIpaTestClient.usedImages(), ForbiddenException.class, expectedMessage("You have no access to this resource."))
                .validate();
        return testDto;
    }

    private <T extends CloudbreakTestDto> T verifyImagesAreUsed(TestContext testContext, T testDto, CloudbreakUser internalActor) {
        testContext
                .as(internalActor)

                .given(UsedImagesTestDto.class)
                .when(utilTestClient.usedImages())
                .then((tc, usedImagesTestDto, client) -> {
                    List<UsedImageStacksV4Response> usedImages = usedImagesTestDto.getResponse().getUsedImages();
                    UsedImageStacksV4Response usedImageStacksV4Response = usedImages.stream()
                            .filter(usedImage -> usedImage.getImage().getImageId().contains(SDX_IMAGE_UUID))
                            .findFirst().orElseThrow(() -> new TestFailException(String.format("SDX image is NOT in use with ID:: %s", SDX_IMAGE_UUID)));
                    LOGGER.info("Used SDX image ID:: {}", usedImageStacksV4Response.getImage().getImageId());
                    return usedImagesTestDto;
                })

                .given(FreeipaUsedImagesTestDto.class)
                .when(freeIpaTestClient.usedImages())
                .then((tc, usedImagesTestDto, client) -> {
                    List<UsedImageStacksV1Response> usedImages = usedImagesTestDto.getResponse().getUsedImages();
                    UsedImageStacksV1Response usedImageStacksV1Response = usedImages.stream()
                            .filter(usedImage -> usedImage.getImage().getImageId().contains(FREEIPA_IMAGE_UUID))
                            .findFirst().orElseThrow(() -> new TestFailException(String.format("FreeIpa image is NOT in use with ID:: %s",
                            FREEIPA_IMAGE_UUID)));
                    LOGGER.info("Used FreeIpa image ID:: {}", usedImageStacksV1Response.getImage().getImageId());
                    return usedImagesTestDto;
                })
                .validate();
        testContext.as(cloudbreakActor.defaultUser());
        return testDto;
    }

    private <T extends CloudbreakTestDto> T verifyImagesAreNotUsed(TestContext testContext, T testDto, CloudbreakUser internalActor) {
        testContext
                .as(internalActor)

                .given(UsedImagesTestDto.class)
                .when(utilTestClient.usedImages())
                .then((tc, usedImagesTestDto, client) -> {
                    List<UsedImageStacksV4Response> usedImages = usedImagesTestDto.getResponse().getUsedImages();
                    if (usedImages.stream()
                            .noneMatch(usedImage -> usedImage.getImage().getImageId().contains(SDX_IMAGE_UUID))) {
                        LOGGER.info("SDX image (ID:: {}) is not in use anymore", SDX_IMAGE_UUID);
                    } else {
                        throw new TestFailException(String.format("SDX image (ID:: %s) is still in use!", SDX_IMAGE_UUID));
                    }
                    return usedImagesTestDto;
                })

                .given(FreeipaUsedImagesTestDto.class)
                .when(freeIpaTestClient.usedImages())
                .then((tc, usedImagesTestDto, client) -> {
                    List<UsedImageStacksV1Response> usedImages = usedImagesTestDto.getResponse().getUsedImages();
                    if (usedImages.stream()
                            .noneMatch(usedImage -> usedImage.getImage().getImageId().contains(FREEIPA_IMAGE_UUID))) {
                        LOGGER.info("FreeIpa image (ID:: {}) is not in use anymore", FREEIPA_IMAGE_UUID);
                    } else {
                        throw new TestFailException(String.format("FreeIpa image (ID:: %s) is still in use!", FREEIPA_IMAGE_UUID));
                    }
                    return usedImagesTestDto;
                })
                .validate();
        testContext.as(cloudbreakActor.defaultUser());
        return testDto;
    }
}
