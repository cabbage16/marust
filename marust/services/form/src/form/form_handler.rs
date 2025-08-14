use axum::{extract::State, http::StatusCode, Json};
use common::{ApiResponse, AppError};
use infrastructure::auth::AuthUser;

use crate::AppState;

use super::{form_dto::SubmitFormRequest, form_repository::SqlxFormRepository, form_service};

pub async fn submit_form(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Json(payload): Json<SubmitFormRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    let repo = SqlxFormRepository::new(state.pg_pool.clone());
    form_service::submit_form(&repo, auth_user.uuid, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}
