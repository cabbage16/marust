package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.infrastructure.pdf.GeneratePdfService;
import com.bamdoliro.maru.infrastructure.pdf.MergePdfService;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.thymeleaf.ProcessTemplateService;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import com.bamdoliro.maru.shared.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GenerateAllAdmissionTicketUseCaseTest {

    @InjectMocks
    private GenerateAllAdmissionTicketUseCase generateAllAdmissionTicketUseCase;

    @Mock
    private ProcessTemplateService processTemplateService;

    @Mock
    private GeneratePdfService generatePdfService;

    @Mock
    private FileService fileService;

    @Mock
    private MergePdfService mergePdfService;

    @Mock
    private FormRepository formRepository;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 모든_1차_합격자의_수험표를_생성한다() {
        // given
        List<Form> formList = new ArrayList<>();
        formList.add(FormFixture.createForm(FormType.REGULAR));
        formList.add(FormFixture.createForm(FormType.MEISTER_TALENT));
        formList.forEach(Form::firstPass);
        given(scheduleService.getAdmissionYear()).willReturn(LocalDate.now().plusYears(1).getYear());
        givenScheduleProperties();
        given(formRepository.findByStatus(FormStatus.FIRST_PASSED)).willReturn(formList);
        given(processTemplateService.execute(any(String.class), anyMap())).willReturn("html");
        given(fileService.getDownloadPresignedUrl(any(String.class), any(String.class))).willReturn(SharedFixture.createIdentificationPictureUrlResponse().getDownloadUrl());
        given(generatePdfService.execute(any(String.class))).willReturn(new ByteArrayOutputStream());

        // when
        generateAllAdmissionTicketUseCase.execute();

        //then
        verify(scheduleService, times(2)).getAdmissionYear();
        verifyScheduleProperties();
        verify(formRepository, times(1)).findByStatus(FormStatus.FIRST_PASSED);
        verify(processTemplateService, times(2)).execute(any(String.class), anyMap());
        verify(generatePdfService, times(2)).execute(any(String.class));
        verify(fileService, times(2)).getDownloadPresignedUrl(any(String.class), any(String.class));
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
        verify(scheduleProperties, times(2)).getCodingTest();
        verify(scheduleProperties, times(2)).getNcs();
        verify(scheduleProperties, times(2)).getDepthInterview();
        verify(scheduleProperties, times(2)).getPhysicalExamination();
        verify(scheduleProperties, times(2)).getAnnouncementOfSecondPass();
        verify(scheduleProperties, times(2)).getMeisterTalentEntranceTime();
        verify(scheduleProperties, times(2)).getMeisterTalentExclusionEntranceTime();
        verify(scheduleProperties, times(2)).getEntranceRegistrationPeriodStart();
        verify(scheduleProperties, times(2)).getEntranceRegistrationPeriodEnd();
    }
}
