package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFileException;
import com.bamdoliro.maru.domain.form.exception.WrongScoreException;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@UseCase
public class UpdateSecondRoundScoreUseCase {

    private final FormRepository formRepository;

    @Transactional
    public void execute(MultipartFile xlsx) throws IOException {
        Workbook workbook = new XSSFWorkbook(xlsx.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<Form> formList = formRepository.findByStatus(FormStatus.FIRST_PASSED);
        formList.sort(Comparator.comparing(Form::getExaminationNumber));
        List<SecondScoreVo> secondScoreVoList = getSecondScoreVoList(sheet);
        validateList(formList, secondScoreVoList);

        for (int index = 0; index < formList.size(); index++) {
            Form form = formList.get(index);
            SecondScoreVo secondScoreVo = secondScoreVoList.get(index);
            validate(form, secondScoreVo);

            updateFormOrNoShow(form, secondScoreVo);
        }

        workbook.close();
    }

    private List<SecondScoreVo> getSecondScoreVoList(Sheet sheet) {
        return IntStream.range(1, sheet.getPhysicalNumberOfRows())
                .mapToObj(sheet::getRow)
                .map(this::getSecondScoreFrom)
                .sorted(Comparator.comparingLong(SecondScoreVo::getExaminationNumber))
                .toList();
    }

    private SecondScoreVo getSecondScoreFrom(Row row) {
        validateRow(row);

        boolean isShow = row.getCell(6).getBooleanCellValue();
        FormType.Category type = getFormType(row.getCell(2).getStringCellValue());

        return new SecondScoreVo(
                (long) row.getCell(0).getNumericCellValue(),
                type,
                isShow ? row.getCell(3).getNumericCellValue() : null,
                isShow ? row.getCell(4).getNumericCellValue() : null,
                isShow && type == FormType.Category.MEISTER_TALENT ? row.getCell(5).getNumericCellValue() : null,
                isShow
        );
    }

    private void validateRow(Row row) {
        // 수험번호 | 이름 | 전형 구분 | 심층면접 | NCS | 코딩테스트 | 응시 여부
        Cell examinationNumberCell = row.getCell(0);
        Cell nameCell = row.getCell(1);
        Cell typeCell = row.getCell(2);
        Cell depthInterviewScoreCell = row.getCell(3);
        Cell ncsScoreCell = row.getCell(4);
        Cell codingTestScoreCell = row.getCell(5);
        Cell isShowCell = row.getCell(6);

        List<Integer> invalidColumns = new ArrayList<>();

        if (examinationNumberCell.getCellType() != CellType.NUMERIC) invalidColumns.add(examinationNumberCell.getColumnIndex()+1);
        if (nameCell.getCellType() != CellType.STRING) invalidColumns.add(nameCell.getColumnIndex()+1);
        if (typeCell.getCellType() != CellType.STRING) invalidColumns.add(typeCell.getColumnIndex()+1);
        if (isShowCell.getCellType() != CellType.BOOLEAN) invalidColumns.add(isShowCell.getColumnIndex()+1);

        boolean isShow = isShowCell.getBooleanCellValue();

        if (isShow) {
            if (depthInterviewScoreCell.getCellType() != CellType.NUMERIC) invalidColumns.add(depthInterviewScoreCell.getColumnIndex()+1);
            if (ncsScoreCell.getCellType() != CellType.NUMERIC) invalidColumns.add(ncsScoreCell.getColumnIndex()+1);
            if (!(codingTestScoreCell.getCellType() == CellType.NUMERIC || codingTestScoreCell.getCellType() == CellType.BLANK)) {
                invalidColumns.add(codingTestScoreCell.getColumnIndex()+1);
            }
        }

        if (!invalidColumns.isEmpty()) {
            String columns = invalidColumns.stream()
                    .map(col -> col + "열")
                    .collect(Collectors.joining(", "));
            throw new InvalidFileException(row.getRowNum()+1 + "행 " + columns + "의 셀타입이 올바르지 않습니다.");
        }

        if (isShow) {
            String type = typeCell.getStringCellValue();
            double depthInterviewScore = depthInterviewScoreCell.getNumericCellValue();
            double ncsScore = ncsScoreCell.getNumericCellValue();
            double codingTestScore = codingTestScoreCell.getNumericCellValue();

            List<Integer> wrongScoreColumns = new ArrayList<>();

            switch (type) {
                case "마이스터인재전형" -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 120)
                        wrongScoreColumns.add(depthInterviewScoreCell.getColumnIndex()+1);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreColumns.add(ncsScoreCell.getColumnIndex()+1);
                    if (codingTestScore < 0 || codingTestScore > 80)
                        wrongScoreColumns.add(codingTestScoreCell.getColumnIndex()+1);
                }
                case "사회통합전형" -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 200)
                        wrongScoreColumns.add(depthInterviewScoreCell.getColumnIndex()+1);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreColumns.add(ncsScoreCell.getColumnIndex()+1);
                }
                // 일반전형, 국가보훈대상자 중 교육지원대상자녀, 특례입학대상자
                default -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 120)
                        wrongScoreColumns.add(depthInterviewScoreCell.getColumnIndex()+1);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreColumns.add(ncsScoreCell.getColumnIndex()+1);
                }
            }

            if (!wrongScoreColumns.isEmpty()) {
                String columns = wrongScoreColumns.stream()
                        .map(col -> col + "열")
                        .collect(Collectors.joining(", "));
                throw new WrongScoreException(row.getRowNum()+1, columns);
            }
        }
    }

    private FormType.Category getFormType(String description) {
        try {
            return FormType.Category.valueOfDescription(description);
        } catch (IllegalArgumentException e) {
            throw new InvalidFileException("지원 전형이 올바르지 않습니다.");
        }
    }

    private static void validateList(List<Form> formList, List<SecondScoreVo> secondScoreVoList) {
        if (formList.size() != secondScoreVoList.size()) {
            throw new InvalidFileException("학생 수가 맞지 않습니다.");
        }
    }

    private void validate(Form form, SecondScoreVo secondScoreVo) {
        if (!form.getExaminationNumber().equals(secondScoreVo.getExaminationNumber())) {
            throw new InvalidFileException("수험번호가 올바르지 않습니다.");
        }
    }

    private void updateFormOrNoShow(Form form, SecondScoreVo secondScoreVo) {
        if (secondScoreVo.isShow()) {
            updateFormSecondRoundScore(form, secondScoreVo);
        } else {
            form.noShow();
        }
    }

    private void updateFormSecondRoundScore(Form form, SecondScoreVo secondScoreVo) {
        if (secondScoreVo.getType().equals(FormType.Category.MEISTER_TALENT)) {
            form.getScore().updateSecondRoundMeisterScore(
                    secondScoreVo.getDepthInterviewScore(),
                    secondScoreVo.getNcsScore(),
                    secondScoreVo.getCodingTestScore()
            );
        } else {
            form.getScore().updateSecondRoundScore(
                    secondScoreVo.getDepthInterviewScore(),
                    secondScoreVo.getNcsScore()
            );
        }
    }
}

@Getter
@AllArgsConstructor
class SecondScoreVo {
    private Long examinationNumber;
    private FormType.Category type;
    private Double depthInterviewScore;
    private Double ncsScore;
    private Double codingTestScore;
    private boolean isShow;
}