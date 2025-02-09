package com.bamdoliro.maru.infrastructure.s3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @NotBlank(message = "필수값입니다.")
    private String fileName;

    @NotNull(message = "필수값입니다.")
    private MediaType mediaType;

    @NotNull(message = "필수값입니다.")
    private Long fileSize;
}
