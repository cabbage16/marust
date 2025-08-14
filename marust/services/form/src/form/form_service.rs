use super::form_dto::*;
use super::form_repository::{FormEntity, SqlxFormRepository, SubjectEntity};
use common::AppError;
use uuid::Uuid;

const MAX_BONUS_SCORE: i32 = 4;

const DEFAULT_ATTENDANCE_SCORE: i32 = 14;
const MIN_ATTENDANCE_SCORE: i32 = 0;
const MAX_ATTENDANCE_SCORE: i32 = 18;
const MAX_ABSENCE_COUNT: i32 = 16;

const DEFAULT_VOLUNTEER_SCORE: i32 = 14;
const MIN_VOLUNTEER_SCORE: i32 = 0;
const MAX_VOLUNTEER_SCORE: i32 = 18;
const MIN_VOLUNTEER_TIME: i32 = 15;
const MAX_VOLUNTEER_TIME: i32 = 30;

const REGULAR_TYPE_DEFAULT_SCORE: f64 = 80.0;
const SPECIAL_TYPE_DEFAULT_SCORE: f64 = 48.0;

pub async fn submit_form(
    repo: &SqlxFormRepository,
    user_uuid: Uuid,
    req: SubmitFormRequest,
) -> Result<(), AppError> {
    let user_id = repo
        .find_user_id_by_uuid(user_uuid)
        .await
        .map_err(|e| AppError::InternalServerError(e.to_string()))?;

    if repo
        .exists_by_user(user_id)
        .await
        .map_err(|e| AppError::InternalServerError(e.to_string()))?
    {
        return Err(AppError::BadRequest("form already submitted".into()));
    }

    let subjects = subject_list_from_requests(&req.grade.subject_list);
    let score = calculate_form_score(&req, &subjects);

    let start = match req.r#type.category() {
        FormCategory::Regular => 1000,
        FormCategory::MeisterTalent => 2000,
        FormCategory::SocialIntegration => 3000,
        FormCategory::Supernumerary => 4000,
    };
    let examination_number = repo
        .next_examination_number(start)
        .await
        .map_err(|e| AppError::InternalServerError(e.to_string()))?;

    let a1 = req.grade.attendance1.as_ref();
    let a2 = req.grade.attendance2.as_ref();
    let a3 = req.grade.attendance3.as_ref();

    let entity = FormEntity {
        birthday: req.applicant.birthday.to_string(),
        gender: format!("{:?}", req.applicant.gender),
        name: req.applicant.name,
        phone_number: req.applicant.phone_number,
        changed_to_regular: false,
        cover_letter: req.document.cover_letter,
        statement_of_purpose: req.document.statement_of_purpose,
        graduation_type: format!("{:?}", req.education.graduation_type),
        graduation_year: req.education.graduation_year,
        school_address: req.education.school_address,
        school_code: req.education.school_code,
        school_location: req.education.school_location,
        school_name: req.education.school_name,
        teacher_mobile_phone_number: req.education.teacher_mobile_phone_number,
        teacher_name: req.education.teacher_name,
        teacher_phone_number: req.education.teacher_phone_number,
        examination_number,
        attendance1_absence_count: a1.map(|v| v.absence_count),
        attendance1_class_absence_count: a1.map(|v| v.class_absence_count),
        attendance1_early_leave_count: a1.map(|v| v.early_leave_count),
        attendance1_lateness_count: a1.map(|v| v.lateness_count),
        attendance2_absence_count: a2.map(|v| v.absence_count),
        attendance2_class_absence_count: a2.map(|v| v.class_absence_count),
        attendance2_early_leave_count: a2.map(|v| v.early_leave_count),
        attendance2_lateness_count: a2.map(|v| v.lateness_count),
        attendance3_absence_count: a3.map(|v| v.absence_count),
        attendance3_class_absence_count: a3.map(|v| v.class_absence_count),
        attendance3_early_leave_count: a3.map(|v| v.early_leave_count),
        attendance3_lateness_count: a3.map(|v| v.lateness_count),
        volunteer_time1: req.grade.volunteer_time1,
        volunteer_time2: req.grade.volunteer_time2,
        volunteer_time3: req.grade.volunteer_time3,
        original_type: Some(format!("{:?}", req.r#type)),
        address: req.parent.address,
        detail_address: req.parent.detail_address,
        zone_code: req.parent.zone_code,
        parent_name: req.parent.name,
        parent_phone_number: req.parent.phone_number,
        parent_relation: req.parent.relation,
        attendance_score: score.attendance_score,
        bonus_score: score.bonus_score,
        coding_test_score: None,
        depth_interview_score: None,
        first_round_score: score.first_round_score,
        ncs_score: None,
        subject_grade_score: score.subject_grade_score,
        third_grade_first_semester_subject_grade_score: score
            .third_grade_first_semester_subject_grade_score,
        total_score: None,
        volunteer_score: score.volunteer_score,
        r#type: format!("{:?}", req.r#type),
        user_id,
    };

    let form_id = repo
        .insert(&entity)
        .await
        .map_err(|e| AppError::InternalServerError(e.to_string()))?;

    let subject_entities = subject_entity_list_from_requests(&req.grade.subject_list);
    repo
        .insert_subjects(form_id, &subject_entities)
        .await
        .map_err(|e| AppError::InternalServerError(e.to_string()))?;

    Ok(())
}

struct Score {
    subject_grade_score: f64,
    third_grade_first_semester_subject_grade_score: Option<f64>,
    attendance_score: i32,
    volunteer_score: i32,
    bonus_score: i32,
    first_round_score: f64,
}

fn calculate_form_score(req: &SubmitFormRequest, subjects: &[Subject]) -> Score {
    let subject_grade_score = calculate_subject_grade_score(req, subjects);
    let attendance_score = calculate_attendance_score(req);
    let volunteer_score = calculate_volunteer_score(req);
    let bonus_score = calculate_bonus_score(req);
    let third_grade_first = if matches!(
        req.education.graduation_type,
        GraduationType::QualificationExamination
    ) {
        None
    } else {
        Some(round_to(subject_map_score(subjects, 3, 1)))
    };
    let first_round_score = round_to(
        subject_grade_score + attendance_score as f64 + volunteer_score as f64 + bonus_score as f64,
    );
    Score {
        subject_grade_score: round_to(subject_grade_score),
        third_grade_first_semester_subject_grade_score: third_grade_first,
        attendance_score,
        volunteer_score,
        bonus_score,
        first_round_score,
    }
}

fn calculate_subject_grade_score(req: &SubmitFormRequest, subjects: &[Subject]) -> f64 {
    if req.r#type.is_regular() || req.r#type.is_supernumerary() {
        calculate_regular_score(req, subjects)
    } else if req.r#type.is_special() {
        calculate_special_score(req, subjects)
    } else {
        0.0
    }
}

fn calculate_regular_score(req: &SubmitFormRequest, subjects: &[Subject]) -> f64 {
    if matches!(req.education.graduation_type, GraduationType::QualificationExamination) {
        let avg = average_score(subjects);
        REGULAR_TYPE_DEFAULT_SCORE + 12.0 * 2.0 * avg
    } else {
        let s21 = subject_map_score(subjects, 2, 1);
        let s22 = subject_map_score(subjects, 2, 2);
        let s31 = subject_map_score(subjects, 3, 1);
        REGULAR_TYPE_DEFAULT_SCORE + 4.8 * (s21 + s22) + 7.2 * 2.0 * s31
    }
}

fn calculate_special_score(req: &SubmitFormRequest, subjects: &[Subject]) -> f64 {
    if matches!(req.education.graduation_type, GraduationType::QualificationExamination) {
        let avg = average_score(subjects);
        SPECIAL_TYPE_DEFAULT_SCORE + 7.2 * 2.0 * avg
    } else {
        let s21 = subject_map_score(subjects, 2, 1);
        let s22 = subject_map_score(subjects, 2, 2);
        let s31 = subject_map_score(subjects, 3, 1);
        SPECIAL_TYPE_DEFAULT_SCORE + 2.88 * (s21 + s22) + 4.32 * 2.0 * s31
    }
}

fn calculate_attendance_score(req: &SubmitFormRequest) -> i32 {
    if matches!(req.education.graduation_type, GraduationType::QualificationExamination) {
        return DEFAULT_ATTENDANCE_SCORE;
    }
    if let Some(total) = get_total_attendance(&req.grade) {
        let converted = total.absence_count
            + ((total.lateness_count + total.early_leave_count + total.class_absence_count) / 3);
        if converted > MAX_ABSENCE_COUNT {
            MIN_ATTENDANCE_SCORE
        } else {
            MAX_ATTENDANCE_SCORE - converted
        }
    } else {
        DEFAULT_ATTENDANCE_SCORE
    }
}

fn calculate_volunteer_score(req: &SubmitFormRequest) -> i32 {
    if matches!(req.education.graduation_type, GraduationType::QualificationExamination)
        || req.grade.volunteer_time1.is_none()
        || req.grade.volunteer_time2.is_none()
        || req.grade.volunteer_time3.is_none()
    {
        return DEFAULT_VOLUNTEER_SCORE;
    }
    let total = req.grade.volunteer_time1.unwrap()
        + req.grade.volunteer_time2.unwrap()
        + req.grade.volunteer_time3.unwrap();
    if total < MIN_VOLUNTEER_TIME {
        MIN_VOLUNTEER_SCORE
    } else if total > MAX_VOLUNTEER_TIME {
        MAX_VOLUNTEER_SCORE
    } else {
        (MAX_VOLUNTEER_SCORE as f64 - ((MAX_VOLUNTEER_TIME - total) as f64 * 0.5)).round() as i32
    }
}

fn calculate_bonus_score(req: &SubmitFormRequest) -> i32 {
    if let Some(list) = &req.grade.certificate_list {
        let sum: i32 = list.iter().map(|c| c.score()).sum();
        sum.min(MAX_BONUS_SCORE)
    } else {
        0
    }
}

fn round_to(value: f64) -> f64 {
    (value * 1000.0).round() / 1000.0
}

fn subject_list_from_requests(reqs: &[SubjectRequest]) -> Vec<Subject> {
    let mut list = Vec::new();
    for req in reqs {
        if let Some(score) = req.score {
            let level = achievement_level_from_score(score);
            list.push(Subject {
                grade: 0,
                semester: 0,
                subject_name: req.subject_name.clone(),
                achievement_level: level,
            });
        } else {
            if let Some(l) = req.achievement_level21.clone() {
                list.push(Subject {
                    grade: 2,
                    semester: 1,
                    subject_name: req.subject_name.clone(),
                    achievement_level: l,
                });
            }
            if let Some(l) = req.achievement_level22.clone() {
                list.push(Subject {
                    grade: 2,
                    semester: 2,
                    subject_name: req.subject_name.clone(),
                    achievement_level: l,
                });
            }
            if let Some(l) = req.achievement_level31.clone() {
                list.push(Subject {
                    grade: 3,
                    semester: 1,
                    subject_name: req.subject_name.clone(),
                    achievement_level: l,
                });
            }
        }
    }
    list
}

fn subject_entity_list_from_requests(reqs: &[SubjectRequest]) -> Vec<SubjectEntity> {
    let mut list = Vec::new();
    for req in reqs {
        if let Some(score) = req.score {
            let level = achievement_level_from_score(score);
            list.push(SubjectEntity {
                grade: 0,
                semester: 0,
                subject_name: req.subject_name.clone(),
                achievement_level: format!("{:?}", level),
                score: Some(score),
            });
        } else {
            if let Some(l) = req.achievement_level21.clone() {
                list.push(SubjectEntity {
                    grade: 2,
                    semester: 1,
                    subject_name: req.subject_name.clone(),
                    achievement_level: format!("{:?}", l),
                    score: None,
                });
            }
            if let Some(l) = req.achievement_level22.clone() {
                list.push(SubjectEntity {
                    grade: 2,
                    semester: 2,
                    subject_name: req.subject_name.clone(),
                    achievement_level: format!("{:?}", l),
                    score: None,
                });
            }
            if let Some(l) = req.achievement_level31.clone() {
                list.push(SubjectEntity {
                    grade: 3,
                    semester: 1,
                    subject_name: req.subject_name.clone(),
                    achievement_level: format!("{:?}", l),
                    score: None,
                });
            }
        }
    }
    list
}

fn achievement_level_from_score(score: i32) -> AchievementLevel {
    if score >= 90 {
        AchievementLevel::A
    } else if score >= 80 {
        AchievementLevel::B
    } else if score >= 70 {
        AchievementLevel::C
    } else if score >= 60 {
        AchievementLevel::D
    } else {
        AchievementLevel::E
    }
}

#[derive(Clone)]
struct Subject {
    grade: i32,
    semester: i32,
    subject_name: String,
    achievement_level: AchievementLevel,
}

impl Subject {
    fn score(&self) -> i32 {
        let base = match self.achievement_level {
            AchievementLevel::A => 5,
            AchievementLevel::B => 4,
            AchievementLevel::C => 3,
            AchievementLevel::D => 2,
            AchievementLevel::E => 1,
        };
        if self.subject_name == "수학" {
            base * 2
        } else {
            base
        }
    }

    fn count(&self) -> i32 {
        if self.subject_name == "수학" {
            2
        } else {
            1
        }
    }
}

fn average_score(subjects: &[Subject]) -> f64 {
    let total: i32 = subjects.iter().map(|s| s.score()).sum();
    let count: i32 = subjects.iter().map(|s| s.count()).sum();
    if count == 0 { 0.0 } else { total as f64 / count as f64 }
}

fn subject_map_score(subjects: &[Subject], grade: i32, semester: i32) -> f64 {
    let filtered: Vec<&Subject> = subjects
        .iter()
        .filter(|s| s.grade == grade && s.semester == semester)
        .collect();
    if filtered.is_empty() {
        0.0
    } else {
        let total: i32 = filtered.iter().map(|s| s.score()).sum();
        let count: i32 = filtered.iter().map(|s| s.count()).sum();
        total as f64 / count as f64
    }
}

struct Attendance {
    absence_count: i32,
    lateness_count: i32,
    early_leave_count: i32,
    class_absence_count: i32,
}

fn get_total_attendance(grade: &GradeRequest) -> Option<Attendance> {
    match (&grade.attendance1, &grade.attendance2, &grade.attendance3) {
        (Some(a1), Some(a2), Some(a3)) => Some(Attendance {
            absence_count: a1.absence_count + a2.absence_count + a3.absence_count,
            lateness_count: a1.lateness_count + a2.lateness_count + a3.lateness_count,
            early_leave_count: a1.early_leave_count + a2.early_leave_count + a3.early_leave_count,
            class_absence_count:
                a1.class_absence_count + a2.class_absence_count + a3.class_absence_count,
        }),
        _ => None,
    }
}
