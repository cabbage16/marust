use bcrypt::{DEFAULT_COST, hash};
use sqlx::PgPool;
use uuid::Uuid;

use crate::common::AppError;

use super::{dto::SignUpUserRequest, repository};

pub async fn sign_up(pool: &PgPool, req: SignUpUserRequest) -> Result<(), AppError> {
    let exists = repository::exists_by_phone(pool, &req.phone_number)
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

    repository::insert_user(
        pool,
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