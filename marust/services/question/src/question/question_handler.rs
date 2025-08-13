use axum::{
    Json, Router,
    extract::{Path, Query, State},
    http::StatusCode,
    routing::{get, post},
};
use common::{ApiResponse, AppError, Authority};
use infrastructure::auth::AuthUser;
use serde::Deserialize;

use crate::AppState;

use super::{
    question_dto::{
        CreateQuestionRequest, IdResponse, QuestionCategory, QuestionResponse,
        UpdateQuestionRequest,
    },
    question_repository::SqlxQuestionRepository,
    question_service,
};

#[derive(Deserialize)]
struct QueryParams {
    category: Option<QuestionCategory>,
}

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/questions", post(create_question).get(query_question_list))
        .route(
            "/questions/:id",
            get(query_question)
                .put(update_question)
                .delete(delete_question),
        )
}

async fn create_question(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Json(payload): Json<CreateQuestionRequest>,
) -> Result<(StatusCode, Json<ApiResponse<IdResponse>>), AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }

    let repo = SqlxQuestionRepository::new(state.pg_pool.clone());
    let id = question_service::create_question(&repo, payload).await?;
    Ok((
        StatusCode::CREATED,
        Json(ApiResponse::ok(IdResponse { id })),
    ))
}

async fn update_question(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Path(id): Path<i64>,
    Json(payload): Json<UpdateQuestionRequest>,
) -> Result<StatusCode, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let repo = SqlxQuestionRepository::new(state.pg_pool.clone());
    question_service::update_question(&repo, id, payload).await?;
    Ok(StatusCode::NO_CONTENT)
}

async fn query_question_list(
    State(state): State<AppState>,
    Query(query): Query<QueryParams>,
) -> Result<Json<ApiResponse<Vec<QuestionResponse>>>, AppError> {
    let repo = SqlxQuestionRepository::new(state.pg_pool.clone());
    let questions = question_service::get_question_list(&repo, query.category).await?;
    Ok(Json(ApiResponse::ok(questions)))
}

async fn query_question(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<QuestionResponse>>, AppError> {
    let repo = SqlxQuestionRepository::new(state.pg_pool.clone());
    let question = question_service::get_question(&repo, id).await?;
    Ok(Json(ApiResponse::ok(question)))
}

async fn delete_question(
    State(state): State<AppState>,
    auth_user: AuthUser,
    Path(id): Path<i64>,
) -> Result<StatusCode, AppError> {
    if auth_user.authority != Authority::Admin {
        return Err(AppError::Forbidden("forbidden".into()));
    }
    let repo = SqlxQuestionRepository::new(state.pg_pool.clone());
    question_service::delete_question(&repo, id).await?;
    Ok(StatusCode::NO_CONTENT)
}
