use chrono::NaiveDate;
use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Gender {
    Male,
    Female,
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum GraduationType {
    Expected,
    Graduated,
    QualificationExamination,
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum AchievementLevel {
    A,
    B,
    C,
    D,
    E,
}

#[derive(Deserialize, Serialize, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Certificate {
    CraftsmanInformationProcessing,
    CraftsmanInformationEquipmentOperation,
    CraftsmanComputer,
    ComputerSpecialistLevel1,
    ComputerSpecialistLevel2,
    ComputerSpecialistLevel3,
}

impl Certificate {
    pub fn score(&self) -> i32 {
        match self {
            Certificate::CraftsmanInformationProcessing => 4,
            Certificate::CraftsmanInformationEquipmentOperation => 4,
            Certificate::CraftsmanComputer => 4,
            Certificate::ComputerSpecialistLevel1 => 3,
            Certificate::ComputerSpecialistLevel2 => 2,
            Certificate::ComputerSpecialistLevel3 => 1,
        }
    }
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum FormType {
    Regular,
    MeisterTalent,
    NationalBasicLiving,
    NearPoverty,
    NationalVeterans,
    OneParent,
    FromNorthKorea,
    Multicultural,
    TeenHouseholder,
    MultiChildren,
    FarmingAndFishing,
    NationalVeteransEducation,
    SpecialAdmission,
}

impl FormType {
    pub fn category(&self) -> FormCategory {
        match self {
            FormType::Regular => FormCategory::Regular,
            FormType::MeisterTalent => FormCategory::MeisterTalent,
            FormType::NationalBasicLiving
            | FormType::NearPoverty
            | FormType::NationalVeterans
            | FormType::OneParent
            | FormType::FromNorthKorea
            | FormType::Multicultural
            | FormType::TeenHouseholder
            | FormType::MultiChildren
            | FormType::FarmingAndFishing => FormCategory::SocialIntegration,
            FormType::NationalVeteransEducation | FormType::SpecialAdmission =>
                FormCategory::Supernumerary,
        }
    }

    pub fn is_regular(&self) -> bool {
        matches!(self, FormType::Regular)
    }

    pub fn is_special(&self) -> bool {
        !self.is_regular() && !self.is_supernumerary()
    }

    pub fn is_supernumerary(&self) -> bool {
        matches!(self, FormType::NationalVeteransEducation | FormType::SpecialAdmission)
    }
}

#[derive(Clone, Copy, PartialEq, Eq)]
pub enum FormCategory {
    Regular,
    MeisterTalent,
    SocialIntegration,
    Supernumerary,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct ApplicantRequest {
    pub name: String,
    pub phone_number: String,
    pub birthday: NaiveDate,
    pub gender: Gender,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct ParentRequest {
    pub name: String,
    pub phone_number: String,
    pub relation: String,
    pub zone_code: String,
    pub address: String,
    pub detail_address: String,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct EducationRequest {
    pub graduation_type: GraduationType,
    pub graduation_year: String,
    pub school_name: Option<String>,
    pub school_location: Option<String>,
    pub school_address: Option<String>,
    pub school_code: Option<String>,
    pub teacher_name: Option<String>,
    pub teacher_phone_number: Option<String>,
    pub teacher_mobile_phone_number: Option<String>,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct SubjectRequest {
    pub subject_name: String,
    pub achievement_level21: Option<AchievementLevel>,
    pub achievement_level22: Option<AchievementLevel>,
    pub achievement_level31: Option<AchievementLevel>,
    pub score: Option<i32>,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct AttendanceRequest {
    pub absence_count: i32,
    pub lateness_count: i32,
    pub early_leave_count: i32,
    pub class_absence_count: i32,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct GradeRequest {
    pub subject_list: Vec<SubjectRequest>,
    pub attendance1: Option<AttendanceRequest>,
    pub attendance2: Option<AttendanceRequest>,
    pub attendance3: Option<AttendanceRequest>,
    pub volunteer_time1: Option<i32>,
    pub volunteer_time2: Option<i32>,
    pub volunteer_time3: Option<i32>,
    pub certificate_list: Option<Vec<Certificate>>,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct DocumentRequest {
    pub cover_letter: String,
    pub statement_of_purpose: String,
}

#[derive(Deserialize, Serialize, Clone)]
pub struct SubmitFormRequest {
    pub applicant: ApplicantRequest,
    pub parent: ParentRequest,
    pub education: EducationRequest,
    pub grade: GradeRequest,
    pub document: DocumentRequest,
    #[serde(rename = "type")]
    pub r#type: FormType,
}
