package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.FormNotFoundException;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.pdf.GeneratePdfService;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.thymeleaf.ProcessTemplateService;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import com.bamdoliro.maru.shared.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateAdmissionTicketUseCaseTest {

    @InjectMocks
    private GenerateAdmissionTicketUseCase generateAdmissionTicketUseCase;

    @Mock
    private FormFacade formFacade;

    @Mock
    private ProcessTemplateService processTemplateService;

    @Mock
    private GeneratePdfService generatePdfService;

    @Mock
    private FileService fileService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 수험표를_생성한다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.MULTI_CHILDREN);
        form.firstPass();

        given(scheduleService.getAdmissionYear()).willReturn(LocalDate.now().plusYears(1).getYear());
        givenScheduleProperties();
        given(formFacade.getForm(user)).willReturn(form);
        given(processTemplateService.execute(any(String.class), anyMap())).willReturn("html");
        given(fileService.getDownloadPresignedUrl(any(String.class), any(String.class))).willReturn(SharedFixture.createIdentificationPictureUrlResponse().getDownloadUrl());
        given(generatePdfService.execute(any(String.class))).willReturn(new ByteArrayOutputStream());

        // when
        generateAdmissionTicketUseCase.execute(user);

        // then
        verify(scheduleService, times(1)).getAdmissionYear();
        verifyScheduleProperties();
        verify(formFacade, times(1)).getForm(user);
        verify(processTemplateService, times(1)).execute(any(String.class), anyMap());
        verify(generatePdfService, times(1)).execute(any(String.class));
        verify(fileService, times(1)).getDownloadPresignedUrl(any(String.class), any(String.class));
    }

    @Test
    void 수험표를_생성할_때_불합격한_사용자라면_에러가_발생한다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.MULTI_CHILDREN);
        form.firstFail();
        given(formFacade.getForm(user)).willReturn(form);

        // when and then
        assertThrows(InvalidFormStatusException.class, () -> generateAdmissionTicketUseCase.execute(user));

        verify(formFacade, times(1)).getForm(user);
        verify(processTemplateService, never()).execute(any(String.class), anyMap());
        verify(generatePdfService, never()).execute(any(String.class));
    }

    @Test
    void 수험표를_생성할_때_원서를_접수하지_않았다면_에러가_발생한다() {
        // given
        User user = UserFixture.createUser();
        willThrow(new FormNotFoundException()).given(formFacade).getForm(user);

        // when and then
        assertThrows(FormNotFoundException.class, () -> generateAdmissionTicketUseCase.execute(user));

        verify(formFacade, times(1)).getForm(user);
        verify(processTemplateService, never()).execute(any(String.class), anyMap());
        verify(generatePdfService, never()).execute(any(String.class));
    }

    private void givenScheduleProperties() {
        LocalDateTime now = LocalDateTime.now();

        given(scheduleProperties.getCodingTest()).willReturn(now);
        given(scheduleProperties.getNcs()).willReturn(now);
        given(scheduleProperties.getDepthInterview()).willReturn(now);
        given(scheduleProperties.getPhysicalExamination()).willReturn(now);
        given(scheduleProperties.getAnnouncementOfSecondPass()).willReturn(now);
        given(scheduleProperties.getMeisterTalentEntranceTime()).willReturn(now);
        given(scheduleProperties.getMeisterTalentExclusionEntranceTime()).willReturn(now);
        given(scheduleProperties.getEntranceRegistrationPeriodStart()).willReturn(now);
        given(scheduleProperties.getEntranceRegistrationPeriodEnd()).willReturn(now);
    }

    private void verifyScheduleProperties() {
        verify(scheduleProperties, times(1)).getCodingTest();
        verify(scheduleProperties, times(1)).getNcs();
        verify(scheduleProperties, times(1)).getDepthInterview();
        verify(scheduleProperties, times(1)).getPhysicalExamination();
        verify(scheduleProperties, times(1)).getAnnouncementOfSecondPass();
        verify(scheduleProperties, times(1)).getMeisterTalentEntranceTime();
        verify(scheduleProperties, times(1)).getMeisterTalentExclusionEntranceTime();
        verify(scheduleProperties, times(1)).getEntranceRegistrationPeriodStart();
        verify(scheduleProperties, times(1)).getEntranceRegistrationPeriodEnd();
    }
}