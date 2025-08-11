pub mod dto;
pub mod repository;
mod service;

use crate::{
    common::{ApiResponse, AppError},
    infrastructure::persistence::user_repository::SqlxUserRepository,
    AppState,
};
use axum::{extract::State, http::StatusCode, routing::post, Json, Router};
use dto::SignUpUserRequest;

pub fn router() -> Router<AppState> {
    Router::new().route("/users", post(sign_up))
}

pub async fn sign_up(
    State(state): State<AppState>,
    Json(payload): Json<SignUpUserRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    let repo = SqlxUserRepository::new(state.pg_pool.clone());
    service::sign_up(&repo, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}
