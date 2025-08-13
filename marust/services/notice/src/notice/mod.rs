pub mod notice_dto;
pub mod notice_repository;
mod notice_service;

use axum::{
    Json, Router,
    extract::{Multipart, Path, State},
    http::StatusCode,
    routing::{get, post},
};
use common::{ApiResponse, AppError, Authority};
use infrastructure::auth::AuthUser;
use uuid::Uuid;

use crate::AppState;
use notice_dto::{IdResponse, NoticeResponse, NoticeSimpleResponse};
use notice_repository::SqlxNoticeRepository;

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/notices", post(create_notice).get(query_notice_list))
        .route(
            "/notices/:id",
            get(query_notice).put(update_notice).delete(delete_notice),
        )
}

async fn save_multipart(
    multipart: &mut Multipart,
    storage_path: &str,
) -> Result<(String, String, Vec<String>), AppError> {
    let mut title: Option<String> = None;
    let mut content: Option<String> = None;
    let mut file_names = Vec::new();

    while let Some(field) = multipart.next_field().await.map_err(|e| {
        tracing::error!("multipart error: {:?}", e);
        AppError::BadRequest("invalid multipart".into())
    })? {
        let name = field.name().unwrap_or("");
        match name {
            "title" => {
                title = Some(
                    field
                        .text()
                        .await
                        .map_err(|_| AppError::BadRequest("invalid title".into()))?,
                )
            }
            "content" => {
                content = Some(
                    field
                        .text()
                        .await
                        .map_err(|_| AppError::BadRequest("invalid content".into()))?,
                )
            }
            "files" => {
                if let Some(filename) = field.file_name() {
                    let saved_name = format!("{}-{}", Uuid::new_v4(), filename);
                    let path = std::path::Path::new(storage_path).join(&saved_name);
                    tokio::fs::create_dir_all(storage_path).await.map_err(|e| {
                        tracing::error!("failed to create dir: {:?}", e);
                        AppError::InternalServerError
                    })?;
                    let bytes = field.bytes().await.map_err(|e| {
                        tracing::error!("failed to read bytes: {:?}", e);
                        AppError::InternalServerError
                    })?;
                    tokio::fs::write(&path, &bytes).await.map_err(|e| {
                        tracing::error!("failed to save file: {:?}", e);
                        AppError::InternalServerError
                    })?;
                    file_names.push(saved_name);
                }
            }
            _ => {}
        }
    }

    let title = title.ok_or_else(|| AppError::BadRequest("missing title".into()))?;
    let content = content.ok_or_else(|| AppError::BadRequest("missing content".into()))?;

    Ok((title, content, file_names))
}

async fn create_notice(
    State(state): State<AppState>,
    auth_user: AuthUser,
    mut multipart: Multipart,
) -> Result<(StatusCode, Json<ApiResponse<IdResponse>>), AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let (title, content, file_names) = save_multipart(&mut multipart, &state.storage_path).await?;
    let repo = SqlxNoticeRepository::new(state.pg_pool.clone());
    let id = notice_service::create_notice(&repo, title, content, file_names).await?;
    Ok((
        StatusCode::CREATED,
        Json(ApiResponse::ok(IdResponse { id })),
    ))
}

async fn update_notice(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Path(id): Path<i64>,
    mut multipart: Multipart,
) -> Result<StatusCode, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let (title, content, file_names) = save_multipart(&mut multipart, &state.storage_path).await?;
    let repo = SqlxNoticeRepository::new(state.pg_pool.clone());
    let old_files = notice_service::update_notice(&repo, id, title, content, file_names).await?;
    for name in old_files {
        let path = std::path::Path::new(&state.storage_path).join(name);
        let _ = tokio::fs::remove_file(path).await;
    }
    Ok(StatusCode::NO_CONTENT)
}

async fn query_notice_list(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Vec<NoticeSimpleResponse>>>, AppError> {
    let repo = SqlxNoticeRepository::new(state.pg_pool.clone());
    let notices = notice_service::get_notice_list(&repo).await?;
    Ok(Json(ApiResponse::ok(notices)))
}

async fn query_notice(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<NoticeResponse>>, AppError> {
    let repo = SqlxNoticeRepository::new(state.pg_pool.clone());
    let notice = notice_service::get_notice(&repo, id).await?;
    Ok(Json(ApiResponse::ok(notice)))
}

async fn delete_notice(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Path(id): Path<i64>,
) -> Result<StatusCode, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let repo = SqlxNoticeRepository::new(state.pg_pool.clone());
    let files = notice_service::delete_notice(&repo, id).await?;
    for name in files {
        let path = std::path::Path::new(&state.storage_path).join(name);
        let _ = tokio::fs::remove_file(path).await;
    }
    Ok(StatusCode::NO_CONTENT)
}
