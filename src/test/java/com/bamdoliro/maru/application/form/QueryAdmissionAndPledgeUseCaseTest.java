package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.presentation.form.dto.response.AdmissionAndPledgeUrlResponse;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QueryAdmissionAndPledgeUseCaseTest {

    @InjectMocks
    private QueryAdmissionAndPledgeUseCase queryAdmissionAndPledgeUseCase;

    @Mock
    private FormRepository formRepository;

    @Mock
    private FileService fileService;

    @Test
    void 입학등록원_및_금연서약서_url을_조회한다() {
        // given
        List<Long> idList = List.of(1L, 2L, 3L);
        given(formRepository.findFormUrlByFormIdList(idList)).willReturn(List.of(
                FormFixture.createFormUrlVo(),
                FormFixture.createFormUrlVo(),
                FormFixture.createFormUrlVo()
        ));
        given(fileService.getDownloadPresignedUrl(any(String.class), any(String.class))).willReturn(SharedFixture.createAdmissionAndPledgeUrlResponse().getDownloadUrl());

        // when
        List<AdmissionAndPledgeUrlResponse> responseList = queryAdmissionAndPledgeUseCase.execute(idList);

        // then
        assertEquals(idList.size(), responseList.size());

        verify(formRepository, times(1)).findFormUrlByFormIdList(idList);
    }
}
