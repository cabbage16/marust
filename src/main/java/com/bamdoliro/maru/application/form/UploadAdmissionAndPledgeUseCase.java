package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.validator.DefaultValidator;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Set;

@RequiredArgsConstructor
@UseCase
public class UploadAdmissionAndPledgeUseCase {

    private final FileService fileService;
    private final FormFacade formFacade;

    public UrlResponse execute(User user, FileMetadata fileMetadata) {
        Form form = formFacade.getForm(user);
        validate(form);

        return fileService.getPresignedUrl(FolderConstant.ADMISSION_AND_PLEDGE, user.getUuid().toString(), fileMetadata, metadata -> {
            DefaultValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20);
        });
    }

    private void validate(Form form) {
        if(!form.isPassedNow() && !form.isEntered())
            throw new InvalidFormStatusException();
    }
}
