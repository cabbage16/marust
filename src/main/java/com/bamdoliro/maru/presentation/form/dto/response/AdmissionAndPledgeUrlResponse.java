package com.bamdoliro.maru.presentation.form.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdmissionAndPledgeUrlResponse {

    private Long examinationNumber;
    private String name;
    private String admissionAndPledgeUrl;

}
