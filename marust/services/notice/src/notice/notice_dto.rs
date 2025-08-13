use chrono::NaiveDateTime;
use serde::Serialize;

#[derive(Serialize)]
pub struct NoticeSimpleResponse {
    pub id: i64,
    pub title: String,
    pub updated_at: NaiveDateTime,
}

#[derive(Serialize)]
pub struct DownloadFileResponse {
    pub download_url: String,
    pub file_name: String,
}

#[derive(Serialize)]
pub struct NoticeResponse {
    pub title: String,
    pub content: String,
    pub file_list: Vec<DownloadFileResponse>,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

#[derive(Serialize)]
pub struct IdResponse {
    pub id: i64,
}
