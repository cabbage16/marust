pub mod dto;
pub mod repository;
mod service;

use axum::{Json, extract::State, http::StatusCode};

use crate::{
    AppState,
    common::{ApiResponse, AppError},
    infrastructure::{
        auth::jwt_provider::JwtProvider, persistence::token_repository::RedisTokenRepository,
    },
};
use dto::LogInRequest;

pub async fn log_in(
    State(state): State<AppState>,
    Json(payload): Json<LogInRequest>,
) -> Result<(StatusCode, Json<ApiResponse<dto::TokenResponse>>), AppError> {
    let jwt_provider = JwtProvider::new(
        state.jwt.secret_key.clone(),
        state.jwt.access_expiration,
        state.jwt.refresh_expiration,
    );
    let token_repo = RedisTokenRepository::new(state.redis_pool.clone());
    let tokens = service::log_in(&state, &jwt_provider, &token_repo, payload).await?;
    Ok((StatusCode::OK, Json(ApiResponse::ok(tokens))))
}
