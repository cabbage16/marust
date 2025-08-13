use common::AppError;

use super::{
    notice_dto::{DownloadFileResponse, NoticeResponse, NoticeSimpleResponse},
    notice_repository::{NoticeDetailModel, NoticeRepository, NoticeSimpleModel},
};

pub async fn create_notice(
    repo: &impl NoticeRepository,
    title: String,
    content: String,
    file_names: Vec<String>,
) -> Result<i64, AppError> {
    repo.insert(&title, &content, &file_names)
        .await
        .map_err(|e| {
            tracing::error!("failed to insert notice: {:?}", e);
            AppError::InternalServerError
        })
}

pub async fn update_notice(
    repo: &impl NoticeRepository,
    id: i64,
    title: String,
    content: String,
    file_names: Vec<String>,
) -> Result<Vec<String>, AppError> {
    let old_files = repo
        .update(id, &title, &content, &file_names)
        .await
        .map_err(|e| {
            tracing::error!("failed to update notice: {:?}", e);
            AppError::InternalServerError
        })?;
    old_files.ok_or_else(|| AppError::NotFound("notice not found".into()))
}

pub async fn get_notice_list(
    repo: &impl NoticeRepository,
) -> Result<Vec<NoticeSimpleResponse>, AppError> {
    let notices = repo.find_all().await.map_err(|e| {
        tracing::error!("failed to fetch notice list: {:?}", e);
        AppError::InternalServerError
    })?;
    Ok(notices.into_iter().map(to_simple_response).collect())
}

pub async fn get_notice(repo: &impl NoticeRepository, id: i64) -> Result<NoticeResponse, AppError> {
    let notice = repo
        .find_by_id(id)
        .await
        .map_err(|e| {
            tracing::error!("failed to fetch notice: {:?}", e);
            AppError::InternalServerError
        })?
        .ok_or_else(|| AppError::NotFound("notice not found".into()))?;
    Ok(to_detail_response(notice))
}

pub async fn delete_notice(repo: &impl NoticeRepository, id: i64) -> Result<Vec<String>, AppError> {
    let files = repo.delete(id).await.map_err(|e| {
        tracing::error!("failed to delete notice: {:?}", e);
        AppError::InternalServerError
    })?;
    files.ok_or_else(|| AppError::NotFound("notice not found".into()))
}

fn to_simple_response(model: NoticeSimpleModel) -> NoticeSimpleResponse {
    NoticeSimpleResponse {
        id: model.id,
        title: model.title,
        updated_at: model.updated_at,
    }
}

fn to_detail_response(model: NoticeDetailModel) -> NoticeResponse {
    let file_list = model
        .file_names
        .into_iter()
        .map(|name| DownloadFileResponse {
            download_url: format!("/files/{}", name),
            file_name: name,
        })
        .collect();
    NoticeResponse {
        title: model.title,
        content: model.content,
        file_list,
        created_at: model.created_at,
        updated_at: model.updated_at,
    }
}
