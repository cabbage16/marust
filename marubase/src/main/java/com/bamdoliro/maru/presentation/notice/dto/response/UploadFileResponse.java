package com.bamdoliro.maru.presentation.notice.dto.response;

import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadFileResponse {

    private final UrlResponse url;
    private final String fileName;
}
