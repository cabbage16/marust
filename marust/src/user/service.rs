use bcrypt::{hash, DEFAULT_COST};
use uuid::Uuid;

use crate::common::AppError;

use super::{dto::SignUpUserRequest, repository::UserRepository};

pub async fn sign_up(repo: &impl UserRepository, req: SignUpUserRequest) -> Result<(), AppError> {
    let exists = repo
        .exists_by_phone(&req.phone_number)
        .await
        .map_err(|e| {
            tracing::error!("failed to check existing user: {:?}", e);
            AppError::InternalServerError
        })?;

    if exists {
        return Err(AppError::BadRequest("phone number already exists".into()));
    }

    let password_hash = hash(&req.password, DEFAULT_COST).map_err(|e| {
        tracing::error!("failed to hash password: {:?}", e);
        AppError::InternalServerError
    })?;

    repo.insert_user(
        Uuid::new_v4(),
        &req.phone_number,
        &req.name,
        &password_hash,
    )
    .await
    .map_err(|e| {
        tracing::error!("failed to insert user: {:?}", e);
        AppError::InternalServerError
    })?;

    Ok(())
}