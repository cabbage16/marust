use axum::{Json, Router, extract::State, http::StatusCode, routing::post};
use common::{ApiResponse, AppError};
use infrastructure::auth::AuthUser;

use crate::AppState;

use super::{
    user_dto::{SignUpUserRequest, UserResponse},
    user_repository::SqlxUserRepository,
    user_service,
};

pub fn router() -> Router<AppState> {
    Router::new().route("/users", post(sign_up).get(get_user))
}

pub async fn sign_up(
    State(state): State<AppState>,
    Json(payload): Json<SignUpUserRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    let repo = SqlxUserRepository::new(state.pg_pool.clone());
    user_service::sign_up(&repo, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}

pub async fn get_user(
    State(state): State<AppState>,
    auth_user: AuthUser,
) -> Result<Json<ApiResponse<UserResponse>>, AppError> {
    let repo = SqlxUserRepository::new(state.pg_pool.clone());
    let user = user_service::get_user(&repo, auth_user.uuid).await?;
    Ok(Json(ApiResponse::ok(user)))
}
