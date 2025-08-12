pub mod dto;
mod service;
pub mod token_repository;

use axum::{
    Json, Router,
    extract::State,
    http::{HeaderMap, StatusCode},
    routing::post,
};

use crate::AppState;
use common::{ApiResponse, AppError};
use dto::LogInRequest;
use infrastructure::auth::{AuthUser, jwt_provider::JwtProvider};
use token_repository::RedisTokenRepository;

pub fn router() -> Router<AppState> {
    Router::new().route("/auth", post(log_in).patch(refresh_token).delete(log_out))
}

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

pub async fn refresh_token(
    State(state): State<AppState>,
    headers: HeaderMap,
) -> Result<(StatusCode, Json<ApiResponse<dto::TokenResponse>>), AppError> {
    let refresh_token = headers
        .get("Refresh-Token")
        .and_then(|v| v.to_str().ok())
        .ok_or_else(|| AppError::BadRequest("missing Refresh-Token header".into()))?
        .to_string();
    let jwt_provider = JwtProvider::new(
        state.jwt.secret_key.clone(),
        state.jwt.access_expiration,
        state.jwt.refresh_expiration,
    );
    let token_repo = RedisTokenRepository::new(state.redis_pool.clone());
    let token = service::refresh_token(&jwt_provider, &token_repo, refresh_token).await?;
    Ok((StatusCode::OK, Json(ApiResponse::ok(token))))
}

pub async fn log_out(
    State(state): State<AppState>,
    auth_user: AuthUser,
) -> Result<StatusCode, AppError> {
    let token_repo = RedisTokenRepository::new(state.redis_pool.clone());
    service::log_out(&token_repo, auth_user.uuid).await?;
    Ok(StatusCode::NO_CONTENT)
}
