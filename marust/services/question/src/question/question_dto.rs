use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use std::{fmt, str::FromStr};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum QuestionCategory {
    TopQuestion,
    AdmissionProcess,
    SchoolLife,
    SubmitDocument,
}

impl fmt::Display for QuestionCategory {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(match self {
            QuestionCategory::TopQuestion => "TOP_QUESTION",
            QuestionCategory::AdmissionProcess => "ADMISSION_PROCESS",
            QuestionCategory::SchoolLife => "SCHOOL_LIFE",
            QuestionCategory::SubmitDocument => "SUBMIT_DOCUMENT",
        })
    }
}

impl FromStr for QuestionCategory {
    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "TOP_QUESTION" => Ok(QuestionCategory::TopQuestion),
            "ADMISSION_PROCESS" => Ok(QuestionCategory::AdmissionProcess),
            "SCHOOL_LIFE" => Ok(QuestionCategory::SchoolLife),
            "SUBMIT_DOCUMENT" => Ok(QuestionCategory::SubmitDocument),
            _ => Err(()),
        }
    }
}

#[derive(Deserialize)]
pub struct CreateQuestionRequest {
    pub title: String,
    pub content: String,
    pub category: QuestionCategory,
}

#[derive(Deserialize)]
pub struct UpdateQuestionRequest {
    pub title: String,
    pub content: String,
    pub category: QuestionCategory,
}

#[derive(Serialize)]
pub struct QuestionResponse {
    pub id: i64,
    pub title: String,
    pub content: String,
    pub category: QuestionCategory,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

#[derive(Serialize)]
pub struct IdResponse {
    pub id: i64,
}
