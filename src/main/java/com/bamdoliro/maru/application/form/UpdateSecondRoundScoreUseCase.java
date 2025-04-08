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
        List<Cell> invalidCellTypeList = new ArrayList<>();
        List<Cell> wrongScoreCellList = new ArrayList<>();
        List<SecondScoreVo> voList = IntStream.range(1, sheet.getPhysicalNumberOfRows())
                .mapToObj(sheet::getRow)
                .map(row -> getSecondScoreFrom(row, invalidCellTypeList, wrongScoreCellList))
                .collect(Collectors.toList());

        if (!invalidCellTypeList.isEmpty()) {
            throw new InvalidFileException("셀타입이 올바르지 않습니다.\n" + formatCellErrorMessage(invalidCellTypeList));
        }
        if (!wrongScoreCellList.isEmpty()) {
            throw new WrongScoreException(formatCellErrorMessage(wrongScoreCellList));
        }

        voList.sort(Comparator.comparingLong(SecondScoreVo::getExaminationNumber));

        return voList;
    }

    private SecondScoreVo getSecondScoreFrom(Row row, List<Cell> invalidTypeCellList, List<Cell> wrongScoreCellList) {
        List<Cell> invalidTypeCells = validateCellType(row);
        List<Cell> wrongScoreCells = invalidTypeCells.isEmpty() ? validateScore(row) : List.of();
        invalidTypeCellList.addAll(invalidTypeCells);
        wrongScoreCellList.addAll(wrongScoreCells);

        if (invalidTypeCells.isEmpty() && wrongScoreCells.isEmpty()) {
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
        } else {
            return null;
        }
    }

    private List<Cell> validateCellType(Row row) {
        // 수험번호 | 이름 | 전형 구분 | 심층면접 | NCS | 코딩테스트 | 응시 여부
        List<Cell> invalidCellTypeList = new ArrayList<>();

        Cell examinationNumberCell = row.getCell(0);
        Cell nameCell = row.getCell(1);
        Cell typeCell = row.getCell(2);
        Cell depthInterviewScoreCell = row.getCell(3);
        Cell ncsScoreCell = row.getCell(4);
        Cell codingTestScoreCell = row.getCell(5);
        Cell isShowCell = row.getCell(6);
        boolean isShow = false;

        if (examinationNumberCell.getCellType() != CellType.NUMERIC) invalidCellTypeList.add(examinationNumberCell);
        if (nameCell.getCellType() != CellType.STRING) invalidCellTypeList.add(nameCell);
        if (typeCell.getCellType() != CellType.STRING) invalidCellTypeList.add(typeCell);
        if (isShowCell.getCellType() != CellType.FORMULA) {
            invalidCellTypeList.add(isShowCell);
        } else {
            isShow = isShowCell.getBooleanCellValue();
        }

        if (isShow) {
            if (depthInterviewScoreCell.getCellType() != CellType.NUMERIC) invalidCellTypeList.add(depthInterviewScoreCell);
            if (ncsScoreCell.getCellType() != CellType.NUMERIC) invalidCellTypeList.add(ncsScoreCell);
            if (!(codingTestScoreCell.getCellType() == CellType.NUMERIC || codingTestScoreCell.getCellType() == CellType.BLANK)) {
                invalidCellTypeList.add(codingTestScoreCell);
            }
        }

        return invalidCellTypeList;
    }

    private List<Cell> validateScore(Row row) {
        // 수험번호 | 이름 | 전형 구분 | 심층면접 | NCS | 코딩테스트 | 응시 여부
        Cell typeCell = row.getCell(2);
        Cell depthInterviewScoreCell = row.getCell(3);
        Cell ncsScoreCell = row.getCell(4);
        Cell codingTestScoreCell = row.getCell(5);
        boolean isShow = row.getCell(6).getCellType() == CellType.FORMULA && row.getCell(6).getBooleanCellValue();

        List<Cell> wrongScoreCellList = new ArrayList<>();

        if (isShow) {
            String type = typeCell.getStringCellValue();
            double depthInterviewScore = depthInterviewScoreCell.getNumericCellValue();
            double ncsScore = ncsScoreCell.getNumericCellValue();
            double codingTestScore = codingTestScoreCell.getNumericCellValue();

            switch (type) {
                case "마이스터인재전형" -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 120)
                        wrongScoreCellList.add(depthInterviewScoreCell);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreCellList.add(ncsScoreCell);
                    if (codingTestScore < 0 || codingTestScore > 80)
                        wrongScoreCellList.add(codingTestScoreCell);
                }
                case "사회통합전형" -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 200)
                        wrongScoreCellList.add(depthInterviewScoreCell);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreCellList.add(ncsScoreCell);
                }
                // 일반전형, 국가보훈대상자 중 교육지원대상자녀, 특례입학대상자
                default -> {
                    if (depthInterviewScore < 0 || depthInterviewScore > 120)
                        wrongScoreCellList.add(depthInterviewScoreCell);
                    if (ncsScore < 0 || ncsScore > 40)
                        wrongScoreCellList.add(ncsScoreCell);
                }
            }
        }

        return wrongScoreCellList;
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

    private String convertToXlsxColumnName(int colIndex) {
        StringBuilder columnName = new StringBuilder();
        int index = colIndex;

        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        }

        return columnName.toString();
    }

    private String formatCellErrorMessage(List<Cell> cells) {
        return cells.stream()
                .map(cell -> {
                    String cellName = convertToXlsxColumnName(cell.getColumnIndex()) + (cell.getRowIndex() + 1);
                    return String.format("- %s: %s", cellName, cell);
                })
                .collect(Collectors.joining("\n"));
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