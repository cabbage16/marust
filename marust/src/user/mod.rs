pub mod dto;
mod repository;
mod service;

use crate::{
    AppState,
    common::{ApiResponse, AppError},
};
use axum::{Json, extract::State, http::StatusCode};
use dto::SignUpUserRequest;

pub async fn sign_up(
    State(state): State<AppState>,
    Json(payload): Json<SignUpUserRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    service::sign_up(&state.pg_pool, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}