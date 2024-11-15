package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@UseCase
public class UploadAdmissionAndPledgeUseCase {

    private final FileService fileService;
    private final FormFacade formFacade;

    public UrlResponse execute(User user) {
        Form form = formFacade.getForm(user);
        validate(form);

        return fileService.getPresignedUrl(FolderConstant.ADMISSION_AND_PLEDGE, user.getUuid().toString());
    }

    private void validate(Form form) {
        if(!form.isPassedNow() && !form.isEntered())
            throw new InvalidFormStatusException();
    }
}
