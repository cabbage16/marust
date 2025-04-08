package com.bamdoliro.maru.domain.form.exception.error;

import com.bamdoliro.maru.shared.error.ErrorProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FormErrorProperty implements ErrorProperty {

    FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "원서를 찾을 수 없습니다."),
    DRAFT_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "임시 저장된 원서를 찾을 수 없습니다."),
    FORM_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "원서는 한 번만 제출할 수 있습니다."),
    CANNOT_UPDATE_NOT_REJECTED_FORM(HttpStatus.CONFLICT, "반려된 원서만 수정할 수 있습니다."),
    INVALID_FORM_STATUS(HttpStatus.CONFLICT, "원서 상태가 유효하지 않습니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "잘못된 파일입니다: %s"),
    MISSING_TOTAL_SCORE(HttpStatus.CONFLICT, "최종 점수가 입력되지 않은 원서가 존재합니다."),
    WRONG_SCORE(HttpStatus.BAD_REQUEST, "점수 범위를 초과한 셀이 존재합니다:\n%s"),
    OUT_OF_APPLICATION_FORM_PERIOD(HttpStatus.FORBIDDEN, "지금은 원서 접수 기간이 아닙니다."),
    OUT_OF_ADMISSION_AND_PLEDGE_PERIOD(HttpStatus.FORBIDDEN, "지금은 입학 등록원 및 서약서 제출 기간이 아닙니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
