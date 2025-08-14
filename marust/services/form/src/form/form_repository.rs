use sqlx::{PgPool, QueryBuilder};
use uuid::Uuid;

pub struct FormEntity {
    pub birthday: String,
    pub gender: String,
    pub name: String,
    pub phone_number: String,
    pub changed_to_regular: bool,
    pub cover_letter: String,
    pub statement_of_purpose: String,
    pub graduation_type: String,
    pub graduation_year: String,
    pub school_address: Option<String>,
    pub school_code: Option<String>,
    pub school_location: Option<String>,
    pub school_name: Option<String>,
    pub teacher_mobile_phone_number: Option<String>,
    pub teacher_name: Option<String>,
    pub teacher_phone_number: Option<String>,
    pub examination_number: i64,
    pub attendance1_absence_count: Option<i32>,
    pub attendance1_class_absence_count: Option<i32>,
    pub attendance1_early_leave_count: Option<i32>,
    pub attendance1_lateness_count: Option<i32>,
    pub attendance2_absence_count: Option<i32>,
    pub attendance2_class_absence_count: Option<i32>,
    pub attendance2_early_leave_count: Option<i32>,
    pub attendance2_lateness_count: Option<i32>,
    pub attendance3_absence_count: Option<i32>,
    pub attendance3_class_absence_count: Option<i32>,
    pub attendance3_early_leave_count: Option<i32>,
    pub attendance3_lateness_count: Option<i32>,
    pub volunteer_time1: Option<i32>,
    pub volunteer_time2: Option<i32>,
    pub volunteer_time3: Option<i32>,
    pub original_type: Option<String>,
    pub address: String,
    pub detail_address: String,
    pub zone_code: String,
    pub parent_name: String,
    pub parent_phone_number: String,
    pub parent_relation: String,
    pub attendance_score: i32,
    pub bonus_score: i32,
    pub coding_test_score: Option<f64>,
    pub depth_interview_score: Option<f64>,
    pub first_round_score: f64,
    pub ncs_score: Option<f64>,
    pub subject_grade_score: f64,
    pub third_grade_first_semester_subject_grade_score: Option<f64>,
    pub total_score: Option<f64>,
    pub volunteer_score: i32,
    pub r#type: String,
    pub user_id: i64,
}

pub struct SubjectEntity {
    pub grade: i32,
    pub semester: i32,
    pub subject_name: String,
    pub achievement_level: String,
    pub score: Option<i32>,
}

pub struct SqlxFormRepository {
    pool: PgPool,
}

impl SqlxFormRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    pub async fn find_user_id_by_uuid(&self, uuid: Uuid) -> Result<i64, sqlx::Error> {
        sqlx::query_scalar("SELECT user_id FROM tbl_user WHERE uuid = $1")
            .bind(uuid)
            .fetch_one(&self.pool)
            .await
    }

    pub async fn exists_by_user(&self, user_id: i64) -> Result<bool, sqlx::Error> {
        let exists: bool = sqlx::query_scalar(
            "SELECT EXISTS(SELECT 1 FROM tbl_form WHERE user_id = $1)",
        )
        .bind(user_id)
        .fetch_one(&self.pool)
        .await?;
        Ok(exists)
    }

    pub async fn next_examination_number(&self, start: i64) -> Result<i64, sqlx::Error> {
        let max: Option<i64> = sqlx::query_scalar::<_, Option<i64>>(
            "SELECT MAX(examination_number) FROM tbl_form WHERE examination_number >= $1 AND examination_number <= $2",
        )
        .bind(start)
        .bind(start + 1000)
        .fetch_one(&self.pool)
        .await?;
        Ok(max.map_or(start + 1, |m| m + 1))
    }

    pub async fn insert(&self, form: &FormEntity) -> Result<i64, sqlx::Error> {
        let mut qb = QueryBuilder::new(
            "INSERT INTO tbl_form (birthday, gender, name, phone_number, changed_to_regular, cover_letter, statement_of_purpose, graduation_type, graduation_year, school_address, school_code, school_location, school_name, teacher_mobile_phone_number, teacher_name, teacher_phone_number, examination_number, attendance1_absence_count, attendance1_class_absence_count, attendance1_early_leave_count, attendance1_lateness_count, attendance2_absence_count, attendance2_class_absence_count, attendance2_early_leave_count, attendance2_lateness_count, attendance3_absence_count, attendance3_class_absence_count, attendance3_early_leave_count, attendance3_lateness_count, volunteer_time1, volunteer_time2, volunteer_time3, original_type, address, detail_address, zone_code, parent_name, parent_phone_number, parent_relation, attendance_score, bonus_score, coding_test_score, depth_interview_score, first_round_score, ncs_score, subject_grade_score, third_grade_first_semester_subject_grade_score, total_score, volunteer_score, status, type, user_id, created_at, updated_at) VALUES (",
        );
        {
            let mut separated = qb.separated(", ");
            separated.push_bind(&form.birthday);
            separated.push_bind(&form.gender);
            separated.push_bind(&form.name);
            separated.push_bind(&form.phone_number);
            separated.push_bind(form.changed_to_regular);
            separated.push_bind(&form.cover_letter);
            separated.push_bind(&form.statement_of_purpose);
            separated.push_bind(&form.graduation_type);
            separated.push_bind(&form.graduation_year);
            separated.push_bind(&form.school_address);
            separated.push_bind(&form.school_code);
            separated.push_bind(&form.school_location);
            separated.push_bind(&form.school_name);
            separated.push_bind(&form.teacher_mobile_phone_number);
            separated.push_bind(&form.teacher_name);
            separated.push_bind(&form.teacher_phone_number);
            separated.push_bind(form.examination_number);
            separated.push_bind(&form.attendance1_absence_count);
            separated.push_bind(&form.attendance1_class_absence_count);
            separated.push_bind(&form.attendance1_early_leave_count);
            separated.push_bind(&form.attendance1_lateness_count);
            separated.push_bind(&form.attendance2_absence_count);
            separated.push_bind(&form.attendance2_class_absence_count);
            separated.push_bind(&form.attendance2_early_leave_count);
            separated.push_bind(&form.attendance2_lateness_count);
            separated.push_bind(&form.attendance3_absence_count);
            separated.push_bind(&form.attendance3_class_absence_count);
            separated.push_bind(&form.attendance3_early_leave_count);
            separated.push_bind(&form.attendance3_lateness_count);
            separated.push_bind(&form.volunteer_time1);
            separated.push_bind(&form.volunteer_time2);
            separated.push_bind(&form.volunteer_time3);
            separated.push_bind(&form.original_type);
            separated.push_bind(&form.address);
            separated.push_bind(&form.detail_address);
            separated.push_bind(&form.zone_code);
            separated.push_bind(&form.parent_name);
            separated.push_bind(&form.parent_phone_number);
            separated.push_bind(&form.parent_relation);
            separated.push_bind(form.attendance_score);
            separated.push_bind(form.bonus_score);
            separated.push_bind(&form.coding_test_score);
            separated.push_bind(&form.depth_interview_score);
            separated.push_bind(form.first_round_score);
            separated.push_bind(&form.ncs_score);
            separated.push_bind(form.subject_grade_score);
            separated.push_bind(&form.third_grade_first_semester_subject_grade_score);
            separated.push_bind(&form.total_score);
            separated.push_bind(form.volunteer_score);
            separated.push_bind("SUBMITTED");
            separated.push_bind(&form.r#type);
            separated.push_bind(form.user_id);
            separated.push("NOW()");
            separated.push("NOW()");
        }
        qb.push(") RETURNING form_id");
        let id: i64 = qb.build_query_scalar().fetch_one(&self.pool).await?;
        Ok(id)
    }

    pub async fn insert_subjects(
        &self,
        form_id: i64,
        subjects: &[SubjectEntity],
    ) -> Result<(), sqlx::Error> {
        if subjects.is_empty() {
            return Ok(());
        }
        let mut qb = QueryBuilder::new(
            "INSERT INTO tbl_subject (form_id, achievement_level, grade, score, semester, subject_name) ",
        );
        qb.push_values(subjects, |mut b, s| {
            b.push_bind(form_id);
            b.push_bind(&s.achievement_level);
            b.push_bind(s.grade);
            b.push_bind(s.score);
            b.push_bind(s.semester);
            b.push_bind(&s.subject_name);
        });
        qb.build().execute(&self.pool).await?;
        Ok(())
    }
}
