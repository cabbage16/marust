use std::str::FromStr;

use common::AppError;

use super::{
    question_dto::{CreateQuestionRequest, QuestionCategory, QuestionResponse, UpdateQuestionRequest},
    question_repository::{QuestionModel, QuestionRepository},
};

pub async fn create_question(
    repo: &impl QuestionRepository,
    req: CreateQuestionRequest,
) -> Result<i64, AppError> {
    repo.insert(&req.title, &req.content, req.category)
        .await
        .map_err(|e| {
            tracing::error!("failed to insert question: {:?}", e);
            AppError::InternalServerError(e.to_string())
        })
}

pub async fn update_question(
    repo: &impl QuestionRepository,
    id: i64,
    req: UpdateQuestionRequest,
) -> Result<(), AppError> {
    let updated = repo
        .update(id, &req.title, &req.content, req.category)
        .await
        .map_err(|e| {
            tracing::error!("failed to update question: {:?}", e);
            AppError::InternalServerError(e.to_string())
        })?;
    if !updated {
        return Err(AppError::NotFound("question not found".into()));
    }
    Ok(())
}

pub async fn get_question_list(
    repo: &impl QuestionRepository,
    category: Option<QuestionCategory>,
) -> Result<Vec<QuestionResponse>, AppError> {
    let questions = repo.find_by_category(category).await.map_err(|e| {
        tracing::error!("failed to fetch questions: {:?}", e);
        AppError::InternalServerError(e.to_string())
    })?;
    Ok(questions.into_iter().map(to_response).collect())
}

pub async fn get_question(
    repo: &impl QuestionRepository,
    id: i64,
) -> Result<QuestionResponse, AppError> {
    let question = repo
        .find_by_id(id)
        .await
        .map_err(|e| {
            tracing::error!("failed to fetch question: {:?}", e);
            AppError::InternalServerError(e.to_string())
        })?
        .ok_or_else(|| AppError::NotFound("question not found".into()))?;
    Ok(to_response(question))
}

pub async fn delete_question(repo: &impl QuestionRepository, id: i64) -> Result<(), AppError> {
    let deleted = repo.delete(id).await.map_err(|e| {
        tracing::error!("failed to delete question: {:?}", e);
        AppError::InternalServerError(e.to_string())
    })?;
    if !deleted {
        return Err(AppError::NotFound("question not found".into()));
    }
    Ok(())
}

fn to_response(model: QuestionModel) -> QuestionResponse {
    let category =
        QuestionCategory::from_str(&model.category).unwrap_or(QuestionCategory::TopQuestion);
    QuestionResponse {
        id: model.id,
        title: model.title,
        content: model.content,
        category,
        created_at: model.created_at,
        updated_at: model.updated_at,
    }
}
