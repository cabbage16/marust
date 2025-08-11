pub mod dto;
pub mod repository;
mod service;

use crate::{
    common::{ApiResponse, AppError},
    infrastructure::persistence::user_repository::SqlxUserRepository,
    AppState,
};
use axum::{extract::State, http::StatusCode, Json};
use dto::SignUpUserRequest;

pub async fn sign_up(
    State(state): State<AppState>,
    Json(payload): Json<SignUpUserRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    let repo = SqlxUserRepository::new(state.pg_pool.clone());
    service::sign_up(&repo, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}
