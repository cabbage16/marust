use bcrypt::{DEFAULT_COST, hash};
use uuid::Uuid;

use common::{AppError, Authority};

use super::{
    user_dto::{SignUpUserRequest, UserResponse},
    user_repository::UserRepository,
};
use std::str::FromStr;

pub async fn sign_up(repo: &impl UserRepository, req: SignUpUserRequest) -> Result<(), AppError> {
    let exists = repo.exists_by_phone(&req.phone_number).await.map_err(|e| {
        tracing::error!("failed to check existing user: {:?}", e);
        AppError::InternalServerError(e.to_string())
    })?;

    if exists {
        return Err(AppError::BadRequest("phone number already exists".into()));
    }

    let password_hash = hash(&req.password, DEFAULT_COST).map_err(|e| {
        tracing::error!("failed to hash password: {:?}", e);
        AppError::InternalServerError(e.to_string())
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
        AppError::InternalServerError(e.to_string())
    })?;

    Ok(())
}

pub async fn get_user(repo: &impl UserRepository, uuid: Uuid) -> Result<UserResponse, AppError> {
    let user = repo
        .find_by_uuid(uuid)
        .await
        .map_err(|e| {
            tracing::error!("failed to fetch user: {:?}", e);
            AppError::InternalServerError(e.to_string())
        })?
        .ok_or(AppError::InternalServerError("user not found".into()))?;

    let authority = Authority::from_str(&user.authority)
        .map_err(|_| AppError::InternalServerError("invalid authority".into()))?;

    Ok(UserResponse {
        phone_number: user.phone_number,
        name: user.name,
        authority,
    })
}
