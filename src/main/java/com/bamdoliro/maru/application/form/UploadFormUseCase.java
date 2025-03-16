package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationPeriodException;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.validator.DefaultFileValidator;
import com.bamdoliro.maru.shared.annotation.UseCase;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@UseCase
public class UploadFormUseCase {

    private final FileService fileService;
    private final FormRepository formRepository;
    private final ScheduleProperties scheduleProperties;

    public UrlResponse execute(User user, FileMetadata fileMetadata) {
        validateApplicationPeriod(LocalDateTime.now());
        Optional<Form> form = formRepository.findByUser(user);
        form.ifPresent(this::validateFormStatus);

        return fileService.getPresignedUrl(FolderConstant.FORM, user.getUuid().toString(), fileMetadata, metadata ->
                DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20)
        );
    }

    private void validateApplicationPeriod(LocalDateTime now) {
        if (now.isBefore(scheduleProperties.getStart()) || now.isAfter(scheduleProperties.getEnd())) {
            throw new OutOfApplicationPeriodException();
        }
    }

    private void validateFormStatus(Form form) {
        if (!form.isRejected()) {
            throw new InvalidFormStatusException();
        }
    }
}
