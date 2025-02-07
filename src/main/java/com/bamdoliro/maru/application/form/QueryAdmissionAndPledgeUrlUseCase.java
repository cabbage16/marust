package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.presentation.form.dto.response.AdmissionAndPledgeUrlResponse;
import com.bamdoliro.maru.presentation.form.dto.response.FormUrlResponse;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@UseCase
public class QueryAdmissionAndPledgeUrlUseCase {

    private final FormRepository formRepository;
    private final FileService fileService;

    public List<AdmissionAndPledgeUrlResponse> execute(List<Long> formIdList) {
        return formRepository.findFormUrlByFormIdList(formIdList).stream()
                .map(vo -> new AdmissionAndPledgeUrlResponse(
                        vo.getExaminationNumber(),
                        vo.getName(),
                        fileService.getDownloadPresignedUrl(FolderConstant.ADMISSION_AND_PLEDGE, vo.getUuid())
                )).toList();
    }
}
