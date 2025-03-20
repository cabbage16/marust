package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.pdf.GeneratePdfService;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.thymeleaf.ProcessTemplateService;
import com.bamdoliro.maru.infrastructure.thymeleaf.Templates;
import com.bamdoliro.maru.shared.annotation.UseCase;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RequiredArgsConstructor
@UseCase
public class GenerateAdmissionTicketUseCase {

    private final FormFacade formFacade;
    private final ProcessTemplateService processTemplateService;
    private final GeneratePdfService generatePdfService;
    private final FileService fileService;
    private final ScheduleService scheduleService;
    private final ScheduleProperties scheduleProperties;

    public ByteArrayResource execute(User user) {
        Form form = formFacade.getForm(user);
        validateFormStatus(form);

        Map<String, Object> formMap = Map.ofEntries(
                Map.entry("form", form),
                Map.entry("year", scheduleService.getAdmissionYear()),
                Map.entry("codingTest", ScheduleService.toLocaleString(scheduleProperties.getCodingTest())),
                Map.entry("ncs", ScheduleService.toLocaleString(scheduleProperties.getNcs())),
                Map.entry("depthInterview", ScheduleService.toLocaleString(scheduleProperties.getDepthInterview())),
                Map.entry("physicalExamination", ScheduleService.toLocaleString(scheduleProperties.getPhysicalExamination())),
                Map.entry("announcementOfSecondPass", ScheduleService.toLocaleString(scheduleProperties.getAnnouncementOfSecondPass())),
                Map.entry("meisterTalentEntranceTime", ScheduleService.toLocaleString(scheduleProperties.getMeisterTalentEntranceTime())),
                Map.entry("meisterTalentExclusionEntranceTime", ScheduleService.toLocaleString(scheduleProperties.getMeisterTalentExclusionEntranceTime())),
                Map.entry("entranceRegistrationTime", ScheduleService.toLocaleString(scheduleProperties.getEntranceRegistrationPeriodStart(), scheduleProperties.getEntranceRegistrationPeriodEnd())),
                Map.entry("identificationPictureUri", fileService.getDownloadPresignedUrl(FolderConstant.IDENTIFICATION_PICTURE, user.getUuid().toString()))
                );
        String html = processTemplateService.execute(Templates.ADMISSION_TICKET, formMap);
        ByteArrayOutputStream outputStream = generatePdfService.execute(html);

        return new ByteArrayResource(outputStream.toByteArray());
    }

    private void validateFormStatus(Form form) {
        if (!form.isFirstPassedNow()) {
            throw new InvalidFormStatusException();
        }
    }
}
