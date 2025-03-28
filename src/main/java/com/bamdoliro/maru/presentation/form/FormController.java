package com.bamdoliro.maru.presentation.form;

import com.bamdoliro.maru.application.form.*;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.presentation.form.dto.response.*;
import com.bamdoliro.maru.shared.auth.AuthenticationPrincipal;
import com.bamdoliro.maru.shared.auth.Authority;
import com.bamdoliro.maru.shared.response.SingleCommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/forms")
@RestController
public class FormController {

    private final QueryFormUseCase queryFormUseCase;

    @GetMapping("/{form-id}")
    public SingleCommonResponse<FormResponse> getForm(
            @AuthenticationPrincipal(authority = Authority.ALL) User user,
            @PathVariable(name = "form-id") Long formId
    ) {
        return SingleCommonResponse.ok(
                queryFormUseCase.execute(user, formId)
        );
    }
}