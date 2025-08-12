use bcrypt::{DEFAULT_COST, hash};
use uuid::Uuid;

use crate::common::AppError;

use super::{
    authority::Authority,
    dto::{SignUpUserRequest, UserResponse},
    repository::UserRepository,
};
use std::str::FromStr;

pub async fn sign_up(repo: &impl UserRepository, req: SignUpUserRequest) -> Result<(), AppError> {
    let exists = repo.exists_by_phone(&req.phone_number).await.map_err(|e| {
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
        Authority::User,
    )
    .await
    .map_err(|e| {
        tracing::error!("failed to insert user: {:?}", e);
        AppError::InternalServerError
    })?;

    Ok(())
}

pub async fn get_user(repo: &impl UserRepository, uuid: Uuid) -> Result<UserResponse, AppError> {
    let user = repo
        .find_by_uuid(uuid)
        .await
        .map_err(|e| {
            tracing::error!("failed to fetch user: {:?}", e);
            AppError::InternalServerError
        })?
        .ok_or(AppError::InternalServerError)?;

    let authority =
        Authority::from_str(&user.authority).map_err(|_| AppError::InternalServerError)?;

    Ok(UserResponse {
        phone_number: user.phone_number,
        name: user.name,
        authority,
    })
}
