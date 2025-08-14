use axum::{extract::{Path, Query, State}, http::StatusCode, Json};
use common::{ApiResponse, AppError, Authority};
use infrastructure::auth::AuthUser;

use crate::AppState;

use super::{
    form_dto::{FormListQuery, FormResponse, FormSimpleResponse, SubmitFormRequest},
    form_repository::SqlxFormRepository,
    form_service,
};

pub async fn submit_form(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Json(payload): Json<SubmitFormRequest>,
) -> Result<(StatusCode, Json<ApiResponse<()>>), AppError> {
    let repo = SqlxFormRepository::new(state.pg_pool.clone());
    form_service::submit_form(&repo, auth_user.uuid, payload).await?;
    Ok((StatusCode::CREATED, Json(ApiResponse::empty())))
}

pub async fn get_form_list(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Query(query): Query<FormListQuery>,
) -> Result<Json<ApiResponse<Vec<FormSimpleResponse>>>, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let repo = SqlxFormRepository::new(state.pg_pool.clone());
    let forms = form_service::get_form_list(&repo, query).await?;
    Ok(Json(ApiResponse::ok(forms)))
}

pub async fn get_form(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<FormResponse>>, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let repo = SqlxFormRepository::new(state.pg_pool.clone());
    let form = form_service::get_form(&repo, id).await?;
    Ok(Json(ApiResponse::ok(form)))
}

pub async fn get_my_form(
    State(state): State<AppState>,
    auth_user: AuthUser,
) -> Result<Json<ApiResponse<FormResponse>>, AppError> {
    let repo = SqlxFormRepository::new(state.pg_pool.clone());
    let form = form_service::get_my_form(&repo, auth_user.uuid).await?;
    Ok(Json(ApiResponse::ok(form)))
}