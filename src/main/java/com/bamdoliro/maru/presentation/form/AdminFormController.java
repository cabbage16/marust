package com.bamdoliro.maru.presentation.form;

import com.bamdoliro.maru.application.form.*;
import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.presentation.form.dto.request.PassOrFailFormListRequest;
import com.bamdoliro.maru.presentation.form.dto.response.AdmissionAndPledgeUrlResponse;
import com.bamdoliro.maru.presentation.form.dto.response.FormSimpleResponse;
import com.bamdoliro.maru.presentation.form.dto.response.FormUrlResponse;
import com.bamdoliro.maru.shared.auth.Authority;
import com.bamdoliro.maru.shared.auth.annotation.RoleCheck;
import com.bamdoliro.maru.shared.response.CommonResponse;
import com.bamdoliro.maru.shared.response.ListCommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/forms/admin")
@RestController
public class AdminFormController {
    private final ApproveFormUseCase approveFormUseCase;
    private final RejectFormUseCase rejectFormUseCase;
    private final ReceiveFormUseCase receiveFormUseCase;
    private final QuerySubmittedFormUseCase querySubmittedFormUseCase;
    private final QueryAllFormUseCase queryAllFormUseCase;
    private final GenerateAllAdmissionTicketUseCase generateAllAdmissionTicketUseCase;
    private final DownloadSecondRoundScoreFormatUseCase downloadSecondRoundScoreFormatUseCase;
    private final UpdateSecondRoundScoreUseCase updateSecondRoundScoreUseCase;
    private final ExportFinalPassedFormUseCase exportFinalPassedFormUseCase;
    private final ExportFirstRoundResultUseCase exportFirstRoundResultUseCase;
    private final ExportSecondRoundResultUseCase exportSecondRoundResultUseCase;
    private final ExportResultUseCase exportResultUseCase;
    private final PassOrFailFormUseCase passOrFailFormUseCase;
    private final QueryFormUrlUseCase queryFormUrlUseCase;
    private final QueryAdmissionAndPledgeUseCase queryAdmissionAndPledgeUseCase;
    private final SelectSecondPassUseCase selectSecondPassUseCase;


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{form-id}/approve")
    @RoleCheck(Authority.ADMIN)
    public void approveForm(
            @PathVariable(name = "form-id") Long formId
    ) {
        approveFormUseCase.execute(formId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{form-id}/reject")
    @RoleCheck(Authority.ADMIN)
    public void rejectForm(
            @PathVariable(name = "form-id") Long formId
    ) {
        rejectFormUseCase.execute(formId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{form-id}/receive")
    @RoleCheck(Authority.ADMIN)
    public void receiveForm(
            @PathVariable(name = "form-id") Long formId
    ) {
        receiveFormUseCase.execute(formId);
    }

    @GetMapping("/review")
    @RoleCheck(Authority.ADMIN)
    public ListCommonResponse<FormSimpleResponse> getSubmittedFormList() {
        return ListCommonResponse.ok(
                querySubmittedFormUseCase.execute()
        );
    }

    @GetMapping
    @RoleCheck(Authority.ADMIN)
    public ListCommonResponse<FormSimpleResponse> getFormList(
            @RequestParam(name = "status", required = false) FormStatus status,
            @RequestParam(name = "type", required = false) FormType.Category type,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return ListCommonResponse.ok(
                queryAllFormUseCase.execute(status, type, sort)
        );
    }

    @GetMapping("/admission-tickets")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> generateAllAdmissionTicket() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(generateAllAdmissionTicketUseCase.execute());
    }

    @GetMapping("/second-round/format")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> downloadSecondRoundScoreFormatUseCase() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(downloadSecondRoundScoreFormatUseCase.execute());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/second-round/score")
    @RoleCheck(Authority.ADMIN)
    public void updateSecondRoundScore(
            @RequestPart(value = "xlsx") MultipartFile file
    ) throws IOException {
        updateSecondRoundScoreUseCase.execute(file);
    }

    @GetMapping("/xlsx/final-passed")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> exportFinalPassedForm() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exportFinalPassedFormUseCase.execute());
    }

    @GetMapping("/xlsx/first-round")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> exportFirstRoundResult() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exportFirstRoundResultUseCase.execute());
    }

    @GetMapping("/xlsx/second-round")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> exportSecondRoundResult() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exportSecondRoundResultUseCase.execute());
    }

    @GetMapping("/xlsx/result")
    @RoleCheck(Authority.ADMIN)
    public ResponseEntity<Resource> exportResult() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exportResultUseCase.execute());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/second-round/result")
    @RoleCheck(Authority.ADMIN)
    public void passOrFailForm(
            @RequestBody @Valid PassOrFailFormListRequest request
    ) {
        passOrFailFormUseCase.execute(request);
    }

    @GetMapping("/form-url")
    @RoleCheck(Authority.ADMIN)
    public ListCommonResponse<FormUrlResponse> getFormUrl(
            @RequestParam(name = "id-list") List<Long> formIdList
    ) {
        return CommonResponse.ok(
                queryFormUrlUseCase.execute(formIdList)
        );
    }


    @GetMapping("/admission-and-pledges")
    @RoleCheck(Authority.ADMIN)
    public ListCommonResponse<AdmissionAndPledgeUrlResponse> getAdmissionAndPledges(
            @RequestParam(name = "id-list") List<Long> formIdList
    ) {
        return CommonResponse.ok(
                queryAdmissionAndPledgeUseCase.execute(formIdList)
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/second-round/select")
    @RoleCheck(Authority.ADMIN)
    public void selectSecondPass() {
        selectSecondPassUseCase.execute();
    }
}
