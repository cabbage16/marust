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

import java.io.File;
import java.io.IOException;
import java.util.Set;

@ActiveProfiles("test")
@ExtendWith(DatabaseClearExtension.class)
@SpringBootTest
public class GenerateProofOfApplicationUseCaseSpringBootTest {

    @Autowired
    private GenerateProofOfApplicationUseCase generateProofOfApplicationUseCase;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;
    @Autowired
    private CalculateFormScoreService calculateFormScoreService;

    @Test
    void 접수증을_저장한다() throws IOException {
        User user = userRepository.save(UserFixture.createUser());
        Form form = FormFixture.createRandomBusanForm(user);

        File image = new ClassPathResource("images/id-picture.png").getFile();
        String presignedUrl = fileService.getUploadPresignedUrl(FolderConstant.IDENTIFICATION_PICTURE, user.getUuid().toString(), new FileMetadata("id-picture.png", "image/png", image.length()), metadata ->
                DefaultFileValidator.validate(metadata, Set.of(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG), 2)
        );

        UploadImageUtil.execute(presignedUrl, image);

        form.assignExaminationNumber(2004L);
        form.submit();
        calculateFormScoreService.execute(form);
        formRepository.save(form);

        SaveFileUtil.execute(generateProofOfApplicationUseCase.execute(user), SaveFileUtil.PDF);
    }
}
