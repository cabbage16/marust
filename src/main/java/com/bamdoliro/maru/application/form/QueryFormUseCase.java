package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.presentation.form.dto.response.FormResponse;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@UseCase
public class QueryFormUseCase {

    private final FormFacade formFacade;
    private final FileService fileService;

    public FormResponse execute(User user, Long id) {
        Form form = formFacade.getForm(id);
        form.isApplicantOrAdmin(user);

        String uuid = form.getUser().getUuid().toString();
        String identificationPictureUri = fileService.getDownloadPresignedUrl(FolderConstant.IDENTIFICATION_PICTURE, uuid);
        String formUrl = fileService.getDownloadPresignedUrl(FolderConstant.FORM, uuid);

        return new FormResponse(form, identificationPictureUri, formUrl);
    }
}
