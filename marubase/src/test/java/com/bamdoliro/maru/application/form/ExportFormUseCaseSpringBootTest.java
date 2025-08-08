package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.service.CalculateFormScoreService;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.persistence.user.UserRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.validator.DefaultFileValidator;
import com.bamdoliro.maru.shared.config.DatabaseClearExtension;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import com.bamdoliro.maru.shared.util.SaveFileUtil;
import com.bamdoliro.maru.shared.util.UploadImageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@ActiveProfiles("test")
@ExtendWith(DatabaseClearExtension.class)
@SpringBootTest
class ExportFormUseCaseSpringBootTest {

    @Autowired
    private ExportFormUseCase exportFormUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private CalculateFormScoreService calculateFormScoreService;

    @Autowired
    private FileService fileService;

    @Transactional
    @Test
    void 검정고시_원서를_pdf로_저장한다() throws IOException {
        User user = userRepository.save(UserFixture.createUser());
        Form form = FormFixture.createRandomQualificationExaminationForm(user);

        File image = new ClassPathResource("images/id-picture.png").getFile();
        String presignedUrl = fileService.getUploadPresignedUrl(FolderConstant.IDENTIFICATION_PICTURE, user.getUuid().toString(), new FileMetadata("id-picture.png", "image/png", image.length()), metadata ->
                DefaultFileValidator.validate(metadata, Set.of(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG), 2)
        );

        UploadImageUtil.execute(presignedUrl, image);

        calculateFormScoreService.execute(form);
        form.assignExaminationNumber(2004L);
        formRepository.save(form);

        SaveFileUtil.execute(exportFormUseCase.execute(user), SaveFileUtil.PDF);
    }

    @Transactional
    @Test
    void 원서를_pdf로_저장한다() throws IOException {
        User user = userRepository.save(UserFixture.createUser());
        Form form = FormFixture.createRandomBusanForm(user);

        File image = new ClassPathResource("images/id-picture.png").getFile();
        String presignedUrl = fileService.getUploadPresignedUrl(FolderConstant.IDENTIFICATION_PICTURE, user.getUuid().toString(), new FileMetadata("id-picture.png", "image/png", image.length()), metadata ->
                DefaultFileValidator.validate(metadata, Set.of(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG), 2)
        );

        UploadImageUtil.execute(presignedUrl, image);

        calculateFormScoreService.execute(form);
        form.assignExaminationNumber(2004L);
        formRepository.save(form);

        SaveFileUtil.execute(exportFormUseCase.execute(user), SaveFileUtil.PDF);
    }
}