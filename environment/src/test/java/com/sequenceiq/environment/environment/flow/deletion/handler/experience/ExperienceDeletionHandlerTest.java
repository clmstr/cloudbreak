package com.sequenceiq.environment.environment.flow.deletion.handler.experience;

import static com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteHandlerSelectors.DELETE_EXPERIENCE_EVENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.environment.environment.domain.Environment;
import com.sequenceiq.environment.environment.dto.EnvironmentDeletionDto;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;
import com.sequenceiq.environment.environment.flow.deletion.event.EnvClusterDeleteFailedEvent;
import com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteEvent;
import com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteFailedEvent;
import com.sequenceiq.environment.environment.service.EnvironmentService;
import com.sequenceiq.flow.reactor.api.event.BaseNamedFlowEvent;
import com.sequenceiq.flow.reactor.api.event.EventSender;

import reactor.bus.Event;

class ExperienceDeletionHandlerTest {

    private static final String TEST_ACCOUNT_ID = "someAccountId";

    private static final String TEST_ENV_CRN = "someEnvCrn";

    private static final String TEST_ENV_NAME = "someEnvName";

    private static final long TEST_ENV_ID = 1L;

    private static final int ONCE = 1;

    @Mock
    private EventSender mockEventSender;

    @Mock
    private Event.Headers mockEventHeaders;

    @Mock
    private EnvironmentDto mockEnvironmentDto;

    @Mock
    private EntitlementService mockEntitlementService;

    @Mock
    private EnvironmentService mockEnvironmentService;

    @Mock
    private EnvironmentDeletionDto mockEnvironmentDeletionDto;

    @Mock
    private Event<EnvironmentDeletionDto> mockEnvironmentDeletionDtoEvent;

    @Mock
    private EnvironmentExperienceDeletionAction mockEnvironmentExperienceDeletionAction;

    @Captor
    private ArgumentCaptor<BaseNamedFlowEvent> baseNamedFlowEvent;

    private ExperienceDeletionHandler underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockEnvironmentDeletionDtoEvent.getHeaders()).thenReturn(mockEventHeaders);
        when(mockEnvironmentDeletionDtoEvent.getData()).thenReturn(mockEnvironmentDeletionDto);
        when(mockEnvironmentDeletionDto.getResourceId()).thenReturn(TEST_ENV_ID);
        when(mockEnvironmentDeletionDto.getEnvironmentDto()).thenReturn(mockEnvironmentDto);
        when(mockEnvironmentDto.getAccountId()).thenReturn(TEST_ACCOUNT_ID);
        when(mockEnvironmentDto.getResourceCrn()).thenReturn(TEST_ENV_CRN);
        when(mockEnvironmentDto.getName()).thenReturn(TEST_ENV_NAME);
        when(mockEnvironmentDto.getId()).thenReturn(TEST_ENV_ID);
        when(mockEnvironmentDto.getResourceId()).thenReturn(TEST_ENV_ID);

        doAnswer(i -> null).when(mockEventSender).sendEvent(baseNamedFlowEvent.capture(), any(Event.Headers.class));

        underTest = new ExperienceDeletionHandler(mockEventSender, mockEntitlementService, mockEnvironmentService, mockEnvironmentExperienceDeletionAction);
    }

    @Test
    void testSelectorShouldReturnDeleteExperienceSelector() {
        assertEquals(DELETE_EXPERIENCE_EVENT.name(), underTest.selector());
    }

    @Test
    void testAcceptIfEntitlementServiceTellsExperienceDeletionEnabledIsNotEnabledDeleteActionWontHappen() {
        when(mockEntitlementService.isExperienceDeletionEnabled(TEST_ACCOUNT_ID)).thenReturn(false);

        underTest.accept(mockEnvironmentDeletionDtoEvent);

        verify(mockEntitlementService, times(ONCE)).isExperienceDeletionEnabled(any());
        verify(mockEntitlementService, times(ONCE)).isExperienceDeletionEnabled(TEST_ACCOUNT_ID);
        verify(mockEnvironmentExperienceDeletionAction, never()).execute(any());

        EnvDeleteEvent capturedDeleteEvent = (EnvDeleteEvent) baseNamedFlowEvent.getValue();
        assertThat(capturedDeleteEvent.getResourceName()).isEqualTo(TEST_ENV_NAME);
        assertThat(capturedDeleteEvent.getResourceId()).isEqualTo(TEST_ENV_ID);
        assertThat(capturedDeleteEvent.getResourceCrn()).isEqualTo(TEST_ENV_CRN);
        assertThat(capturedDeleteEvent.selector()).isEqualTo("START_DATALAKE_CLUSTERS_DELETE_EVENT");
    }

    @Test
    void testAcceptIfEntitlementServiceTellsExperienceDeletionEnabledIsEnabledButEnvironmentIsNotExistsForIdThenDeleteActionShouldNotHappen() {
        when(mockEntitlementService.isExperienceDeletionEnabled(TEST_ACCOUNT_ID)).thenReturn(false);
        when(mockEnvironmentService.findEnvironmentById(TEST_ENV_ID)).thenReturn(Optional.empty());

        underTest.accept(mockEnvironmentDeletionDtoEvent);

        verify(mockEnvironmentExperienceDeletionAction, never()).execute(any());

        EnvDeleteEvent capturedDeleteEvent = (EnvDeleteEvent) baseNamedFlowEvent.getValue();
        assertThat(capturedDeleteEvent.getResourceName()).isEqualTo(TEST_ENV_NAME);
        assertThat(capturedDeleteEvent.getResourceId()).isEqualTo(TEST_ENV_ID);
        assertThat(capturedDeleteEvent.getResourceCrn()).isEqualTo(TEST_ENV_CRN);
        assertThat(capturedDeleteEvent.selector()).isEqualTo("START_DATALAKE_CLUSTERS_DELETE_EVENT");
    }

    @Test
    void testAcceptIfEntitlementServiceTellsExperienceDeletionEnabledIsEnabledAndEnvironmentIsExistsForIdThenDeleteActionShouldHappen() {
        Environment env = new Environment();
        when(mockEntitlementService.isExperienceDeletionEnabled(TEST_ACCOUNT_ID)).thenReturn(true);
        when(mockEnvironmentService.findEnvironmentById(TEST_ENV_ID)).thenReturn(Optional.of(env));

        underTest.accept(mockEnvironmentDeletionDtoEvent);

        verify(mockEnvironmentExperienceDeletionAction, times(ONCE)).execute(any());
        verify(mockEnvironmentExperienceDeletionAction, times(ONCE)).execute(env);
        verify(mockEventSender, times(ONCE)).sendEvent(any(EnvDeleteEvent.class), eq(mockEventHeaders));
        verify(mockEventSender, never()).sendEvent(any(EnvDeleteFailedEvent.class), any());

        EnvDeleteEvent capturedDeleteEvent = (EnvDeleteEvent) baseNamedFlowEvent.getValue();
        assertThat(capturedDeleteEvent.getResourceName()).isEqualTo(TEST_ENV_NAME);
        assertThat(capturedDeleteEvent.getResourceId()).isEqualTo(TEST_ENV_ID);
        assertThat(capturedDeleteEvent.getResourceCrn()).isEqualTo(TEST_ENV_CRN);
        assertThat(capturedDeleteEvent.selector()).isEqualTo("START_DATALAKE_CLUSTERS_DELETE_EVENT");
    }

    @Test
    void testAcceptWhenDeleteActionThrowsExceptionThenEventSenderShouldAcceptEnvDeleteFailedEvent() {
        Environment env = new Environment();
        when(mockEntitlementService.isExperienceDeletionEnabled(TEST_ACCOUNT_ID)).thenReturn(true);
        when(mockEnvironmentService.findEnvironmentById(TEST_ENV_ID)).thenReturn(Optional.of(env));
        doThrow(new RuntimeException()).when(mockEnvironmentExperienceDeletionAction).execute(env);

        underTest.accept(mockEnvironmentDeletionDtoEvent);

        verify(mockEnvironmentExperienceDeletionAction, times(ONCE)).execute(any());
        verify(mockEnvironmentExperienceDeletionAction, times(ONCE)).execute(env);
        verify(mockEventSender, times(ONCE)).sendEvent(any(EnvClusterDeleteFailedEvent.class), eq(mockEventHeaders));
        verify(mockEventSender, never()).sendEvent(any(EnvDeleteEvent.class), any());

        EnvClusterDeleteFailedEvent capturedDeleteEvent = (EnvClusterDeleteFailedEvent) baseNamedFlowEvent.getValue();
        assertThat(capturedDeleteEvent.getResourceName()).isEqualTo(TEST_ENV_NAME);
        assertThat(capturedDeleteEvent.getResourceId()).isEqualTo(TEST_ENV_ID);
        assertThat(capturedDeleteEvent.getResourceCrn()).isEqualTo(TEST_ENV_CRN);
        assertThat(capturedDeleteEvent.selector()).isEqualTo("FAILED_ENV_CLUSTERS_DELETE_EVENT");
    }

}