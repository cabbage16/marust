package com.bamdoliro.maru.presentation.form;

import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.FormNotFoundException;
import com.bamdoliro.maru.domain.form.exception.InvalidFileException;
import com.bamdoliro.maru.domain.form.exception.MissingTotalScoreException;
import com.bamdoliro.maru.domain.form.exception.WrongScoreException;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.presentation.form.dto.request.PassOrFailFormListRequest;
import com.bamdoliro.maru.presentation.form.dto.request.PassOrFailFormRequest;
import com.bamdoliro.maru.presentation.form.dto.response.FormSimpleResponse;
import com.bamdoliro.maru.shared.fixture.AuthFixture;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import com.bamdoliro.maru.shared.util.RestDocsTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminFormControllerTest extends RestDocsTestSupport {

    @Test
    void 원서를_승인한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(approveFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/approve", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("form-id")
                                        .description("승인할 원서의 id")
                        )
                ));

        verify(approveFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 원서를_승인할_때_원서가_없으면_에러가_발생한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        doThrow(new FormNotFoundException()).when(approveFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/approve", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNotFound())

                .andDo(restDocs.document());

        verify(approveFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 원서를_반려한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(rejectFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/reject", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("form-id")
                                        .description("반려할 원서의 id")
                        )
                ));

        verify(rejectFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 원서를_반려할_때_원서가_없으면_에러가_발생한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        doThrow(new FormNotFoundException()).when(rejectFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/reject", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNotFound())

                .andDo(restDocs.document());

        verify(rejectFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 원서를_접수한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(receiveFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/receive", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        pathParameters(
                                parameterWithName("form-id")
                                        .description("접수할 원서의 id")
                        )
                ));

        verify(receiveFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 원서를_접수할_때_원서가_없으면_에러가_발생한다() throws Exception {
        Long formId = 1L;
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        doThrow(new FormNotFoundException()).when(receiveFormUseCase).execute(formId);


        mockMvc.perform(patch("/forms/{form-id}/receive", formId)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isNotFound())

                .andDo(restDocs.document());

        verify(receiveFormUseCase, times(1)).execute(formId);
    }

    @Test
    void 검토해야_하는_원서를_조회한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(querySubmittedFormUseCase.execute()).willReturn(List.of(
                FormFixture.createFormSimpleResponse(FormStatus.FINAL_SUBMITTED),
                FormFixture.createFormSimpleResponse(FormStatus.REJECTED),
                FormFixture.createFormSimpleResponse(FormStatus.FINAL_SUBMITTED)
        ));


        mockMvc.perform(get("/forms/review")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));
    }

    @Test
    void 검토해야_하는_원서가_없으면_빈_리스트를_반환한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(querySubmittedFormUseCase.execute()).willReturn(List.of());


        mockMvc.perform(get("/forms/review")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isOk())

                .andDo(restDocs.document());
    }

    @Test
    void 원서를_전체_조회한다() throws Exception {
        User user = UserFixture.createUser();
        List<FormSimpleResponse> responseList = List.of(
                FormFixture.createFormSimpleResponse(FormStatus.SUBMITTED),
                FormFixture.createFormSimpleResponse(FormStatus.SUBMITTED),
                FormFixture.createFormSimpleResponse(FormStatus.SUBMITTED),
                FormFixture.createFormSimpleResponse(FormStatus.SUBMITTED),
                FormFixture.createFormSimpleResponse(FormStatus.SUBMITTED)
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(queryAllFormUseCase.execute(FormStatus.SUBMITTED, FormType.Category.REGULAR, null)).willReturn(responseList);

        mockMvc.perform(get("/forms")
                        .param("status", FormStatus.SUBMITTED.name())
                        .param("type", FormType.Category.REGULAR.name())
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        queryParameters(
                                parameterWithName("status")
                                        .description("<<form-status,원서 상태 (null인 경우 전체 조회)>>")
                                        .optional(),
                                parameterWithName("type")
                                        .description("<<form-category,원서 카테고리 (null인 경우 전체 조회)>>")
                                        .optional(),
                                parameterWithName("sort")
                                        .description("정렬 기준 (total-score-asc인 경우 최종 점수 오름차순, total-score-desc인 경우 최종 점수 내림차순, form-id인 경우 접수 번호순, null인 경우 수험번호 오름차순 조회)")
                                        .optional()
                        )
                ));

        verify(queryAllFormUseCase, times(1)).execute(FormStatus.SUBMITTED, FormType.Category.REGULAR, null);
    }

    @Test
    void 수험표_전체를_발급받는다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "admission-ticket-all",
                "admission-ticket-all.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "<<file>>".getBytes()
        );
        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(generateAllAdmissionTicketUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/admission-tickets")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept(MediaType.APPLICATION_PDF))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(generateAllAdmissionTicketUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_2차_전형_점수_양식을_다운로드한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "2차전형점수양식",
                "2차전형점수양식.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(downloadSecondRoundScoreFormatUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/second-round/format")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(downloadSecondRoundScoreFormatUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_2차_전형_점수를_입력한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "xlsx",
                "2차전형점수양식.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(updateSecondRoundScoreUseCase).execute(any(MultipartFile.class));

        mockMvc.perform(multipartPatch("/forms/second-round/score")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .contentType(MediaType.MULTIPART_FORM_DATA))

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        requestParts(
                                partWithName("xlsx")
                                        .description("2차 전형 점수 양식 엑셀 파일")
                        )
                ));

        verify(updateSecondRoundScoreUseCase, times(1)).execute(any(MultipartFile.class));
    }

    @Test
    void 잘못된_양식의_2차_전형_점수를_입력하면_에러가_발생한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "xlsx",
                "2차전형점수양식.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        doThrow(new InvalidFileException()).when(updateSecondRoundScoreUseCase).execute(any(MultipartFile.class));

        mockMvc.perform(multipartPatch("/forms/second-round/score")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .contentType(MediaType.MULTIPART_FORM_DATA))

                .andExpect(status().isBadRequest())

                .andDo(restDocs.document());

        verify(updateSecondRoundScoreUseCase, times(1)).execute(any(MultipartFile.class));
    }

    @Test
    void 입력한_2차_전형_점수가_범위를_초과한_경우_에러가_발생한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "xlsx",
                "2차전형점수양식.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        doThrow(new WrongScoreException()).when(updateSecondRoundScoreUseCase).execute(any(MultipartFile.class));

        mockMvc.perform(multipartPatch("/forms/second-round/score")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .contentType(MediaType.MULTIPART_FORM_DATA))

                .andExpect(status().isBadRequest())

                .andDo(restDocs.document());

        verify(updateSecondRoundScoreUseCase, times(1)).execute(any(MultipartFile.class));
    }

    @Test
    void 정상적으로_최종_합격자를_다운로드한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "최종합격자",
                "최종합격자.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(exportFinalPassedFormUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/xlsx/final-passed")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(exportFinalPassedFormUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_1차전형_결과를_다운로드한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "1차전형결과",
                "1차전형결과.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(exportFinalPassedFormUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/xlsx/first-round")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(exportFirstRoundResultUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_2차전형_결과를_다운로드한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "2차전형결과",
                "2차전형결과.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(exportFinalPassedFormUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/xlsx/second-round")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(exportSecondRoundResultUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_입학전형_전체_결과를_다운로드한다() throws Exception {
        User user = UserFixture.createAdminUser();
        MockMultipartFile file = new MockMultipartFile(
                "전체결과",
                "전체결과.xlsx",
                String.valueOf(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                "<<file>>".getBytes()
        );

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(exportFinalPassedFormUseCase.execute()).willReturn(new ByteArrayResource(file.getBytes()));

        mockMvc.perform(get("/forms/xlsx/result")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        )
                ));

        verify(exportResultUseCase, times(1)).execute();
    }

    @Test
    void 정상적으로_2차_합격_여부를_입력한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(passOrFailFormUseCase).execute(any(PassOrFailFormListRequest.class));

        PassOrFailFormListRequest request = new PassOrFailFormListRequest(
                List.of(
                        new PassOrFailFormRequest(3L, true),
                        new PassOrFailFormRequest(2L, false),
                        new PassOrFailFormRequest(5L, true),
                        new PassOrFailFormRequest(4L, false),
                        new PassOrFailFormRequest(1L, true)
                )
        );

        mockMvc.perform(patch("/forms/second-round/result")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        requestFields(
                                fieldWithPath("formList")
                                        .type(JsonFieldType.ARRAY)
                                        .description("2차 전형 결과를 입력할 원서 목록"),
                                fieldWithPath("formList[].formId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("원서 id"),
                                fieldWithPath("formList[].pass")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("합격 여부")
                        )
                ));

        verify(passOrFailFormUseCase, times(1)).execute(any(PassOrFailFormListRequest.class));
    }

    @Test
    void 어드민이_2차_합격_여부를_입력할_때_존재하지_않는_원서를_입력했다면_에러가_발생한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willThrow(new FormNotFoundException()).given(passOrFailFormUseCase).execute(any(PassOrFailFormListRequest.class));

        PassOrFailFormListRequest request = new PassOrFailFormListRequest(
                List.of(
                        new PassOrFailFormRequest(390L, true)
                )
        );

        mockMvc.perform(patch("/forms/second-round/result")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))

                .andExpect(status().isNotFound())

                .andDo(restDocs.document());

        verify(passOrFailFormUseCase, times(1)).execute(any(PassOrFailFormListRequest.class));
    }

    @Test
    void 선택한_원서의_원서url을_조회한다() throws Exception {
        User user = UserFixture.createAdminUser();
        List<Long> idList = List.of(1L, 2L, 3L, 4L, 5L);

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(queryFormUrlUseCase.execute(idList)).willReturn(
                List.of(FormFixture.createFormUrlResponse(),
                        FormFixture.createFormUrlResponse(),
                        FormFixture.createFormUrlResponse(),
                        FormFixture.createFormUrlResponse(),
                        FormFixture.createFormUrlResponse())
        );

        mockMvc.perform(get("/forms/form-url")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .param("id-list", "1,2,3,4,5")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        queryParameters(
                                parameterWithName("id-list")
                                        .description("조회할 원서 id 목록")
                        )
                ));

        verify(queryFormUrlUseCase, times(1)).execute(idList);
    }

    @Test
    void 자동으로_2차_합격_여부를_결정한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willDoNothing().given(selectSecondPassUseCase).execute();

        mockMvc.perform(patch("/forms/second-round/select")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader()))

                .andExpect(status().isNoContent())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer token")
                        )
                ));

        verify(selectSecondPassUseCase, times(1)).execute();
    }

    @Test
    void 자동으로_2차_합격_여부를_결정할_때_최종_점수가_없는_원서가_존재하면_에러가_발생한다() throws Exception {
        User user = UserFixture.createAdminUser();

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        willThrow(new MissingTotalScoreException()).given(selectSecondPassUseCase).execute();

        mockMvc.perform(patch("/forms/second-round/select")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader()))

                .andExpect(status().isConflict())

                .andDo(restDocs.document());

        verify(selectSecondPassUseCase, times(1)).execute();
    }

    @Test
    void 선택한_원서들의_입학등록원_및_금연서약서url을_조회한다() throws Exception {
        User user = UserFixture.createAdminUser();
        List<Long> idList = List.of(1L, 2L, 3L, 4L, 5L);

        given(authenticationArgumentResolver.supportsParameter(any(MethodParameter.class))).willReturn(true);
        given(authenticationArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);
        given(queryAdmissionAndPledgeUseCase.execute(idList)).willReturn(
                List.of(FormFixture.createAdmissionAndPledgeUrlResponse(),
                        FormFixture.createAdmissionAndPledgeUrlResponse(),
                        FormFixture.createAdmissionAndPledgeUrlResponse(),
                        FormFixture.createAdmissionAndPledgeUrlResponse(),
                        FormFixture.createAdmissionAndPledgeUrlResponse())
        );

        mockMvc.perform(get("/forms/admission-and-pledges")
                        .header(HttpHeaders.AUTHORIZATION, AuthFixture.createAuthHeader())
                        .param("id-list", "1,2,3,4,5")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())

                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer token")
                        ),
                        queryParameters(
                                parameterWithName("id-list")
                                        .description("조회할 원서 id 목록")
                        )
                ));

        verify(queryAdmissionAndPledgeUseCase, times(1)).execute(idList);
    }

}