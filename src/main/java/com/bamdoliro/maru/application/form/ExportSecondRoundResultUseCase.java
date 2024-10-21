package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.xlsx.XlsxService;
import com.bamdoliro.maru.infrastructure.xlsx.constant.XlsxConstant;
import com.bamdoliro.maru.shared.annotation.UseCase;
import com.bamdoliro.maru.shared.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@UseCase
public class ExportSecondRoundResultUseCase {

    private final FormRepository formRepository;
    private final FormFacade formFacade;
    private final XlsxService xlsxService;

    public Resource execute() throws IOException {
        List<Form> formList = formRepository.findSecondRoundForm()
                .stream()
                .sorted(
                        formFacade.getFormComparator()
                                .thenComparing(form -> form.getScore().getTotalScore())
                )
                .toList();

        Workbook workbook = xlsxService.openTemplate("2차전형결과");
        Sheet sheet = workbook.getSheetAt(0);

        CellStyle defaultCellStyle = xlsxService.createDefaultCellStyle(workbook);
        CellStyle rightCellStyle = xlsxService.createRightCellStyle(workbook);
        CellStyle emptyCellStyle = xlsxService.createEmptyCellStyle(workbook);

        for (int index = 0; index < formList.size(); index++) {
            Form form = formList.get(index);
            Row row = sheet.createRow(index + XlsxConstant.FIRST_ROW_INDEX_WITH_TITLE);

            Cell idCell = row.createCell(0);
            idCell.setCellValue(form.getId());
            idCell.setCellStyle(defaultCellStyle);

            Cell examinationNumberCell = row.createCell(1);
            examinationNumberCell.setCellValue(form.getExaminationNumber());
            examinationNumberCell.setCellStyle(defaultCellStyle);

            Cell originalTypeCell = row.createCell(2);
            originalTypeCell.setCellValue(form.getOriginalType().getDescription());
            originalTypeCell.setCellStyle(defaultCellStyle);

            Cell typeCell = row.createCell(3);
            typeCell.setCellValue(form.getType().getDescription());
            typeCell.setCellStyle(defaultCellStyle);

            Cell resultCell = row.createCell(4);
            resultCell.setCellValue(form.getStatus().getDescription());
            resultCell.setCellStyle(defaultCellStyle);

            Cell nameCell = row.createCell(5);
            nameCell.setCellValue(form.getApplicant().getName());
            nameCell.setCellStyle(defaultCellStyle);

            Cell genderCell = row.createCell(6);
            genderCell.setCellValue(form.getApplicant().getGender().getDescription());
            genderCell.setCellStyle(defaultCellStyle);

            Cell birthdayCell = row.createCell(7);
            birthdayCell.setCellValue(form.getApplicant().getBirthday().format(DateTimeFormatter.BASIC_ISO_DATE));
            birthdayCell.setCellStyle(defaultCellStyle);

            Cell locationCell = row.createCell(8);
            locationCell.setCellValue(form.getEducation().getSchool().getLocation());
            locationCell.setCellStyle(defaultCellStyle);

            Cell graduationCell = row.createCell(9);
            graduationCell.setCellValue(form.getEducation().getGraduationTypeToString());
            graduationCell.setCellStyle(defaultCellStyle);

            Cell schoolCell = row.createCell(10);
            schoolCell.setCellValue(form.getEducation().getSchool().getName());
            schoolCell.setCellStyle(defaultCellStyle);

            Cell schoolCodeCell = row.createCell(11);
            schoolCodeCell.setCellValue(form.getEducation().getSchool().getCode());
            schoolCodeCell.setCellStyle(defaultCellStyle);

            Cell subjectGradeScoreCell = row.createCell(12);
            subjectGradeScoreCell.setCellValue(MathUtil.roundTo(form.getScore().getSubjectGradeScore(), 3));
            subjectGradeScoreCell.setCellStyle(rightCellStyle);

            Cell attendanceScoreCell = row.createCell(13);
            attendanceScoreCell.setCellValue(form.getScore().getAttendanceScore());
            attendanceScoreCell.setCellStyle(rightCellStyle);

            Cell volunteerScoreCell = row.createCell(14);
            volunteerScoreCell.setCellValue(form.getScore().getVolunteerScore());
            volunteerScoreCell.setCellStyle(rightCellStyle);

            Cell bonusScoreCell = row.createCell(15);
            bonusScoreCell.setCellValue(form.getScore().getBonusScore());
            bonusScoreCell.setCellStyle(rightCellStyle);

            Cell depthInterviewScoreCell = row.createCell(16);
            depthInterviewScoreCell.setCellValue(form.getScore().getDepthInterviewScore());
            depthInterviewScoreCell.setCellStyle(rightCellStyle);

            Cell ncsScoreCell = row.createCell(17);
            ncsScoreCell.setCellValue(form.getScore().getNcsScore());
            ncsScoreCell.setCellStyle(rightCellStyle);

            Cell codingTestScoreCell = row.createCell(18);
            if (form.getType().isMeister()) {
                codingTestScoreCell.setCellValue(form.getScore().getCodingTestScore());
                codingTestScoreCell.setCellStyle(rightCellStyle);
            } else {
                codingTestScoreCell.setCellStyle(emptyCellStyle);
            }

            Cell totalScoreCell = row.createCell(19);
            totalScoreCell.setCellValue(MathUtil.roundTo(form.getScore().getTotalScore(), 3));
            totalScoreCell.setCellStyle(rightCellStyle);
        }

        return xlsxService.convertToByteArrayResource(workbook);
    }
}
