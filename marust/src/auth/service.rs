use crate::{
    AppState, auth::repository::TokenRepository, common::AppError,
    infrastructure::auth::jwt_provider::JwtProvider,
};
use bcrypt::verify;

use super::dto::{LogInRequest, TokenResponse};

#[derive(sqlx::FromRow)]
struct UserRow {
    uuid: uuid::Uuid,
    name: String,
    phone_number: String,
    password: String,
}

pub async fn log_in(
    state: &AppState,
    jwt_provider: &JwtProvider,
    repo: &impl TokenRepository,
    req: LogInRequest,
) -> Result<TokenResponse, AppError> {
    let user = sqlx::query_as::<_, UserRow>(
        "SELECT uuid, name, phone_number, password FROM tbl_user WHERE phone_number = $1",
    )
    .bind(&req.phone_number)
    .fetch_optional(&state.pg_pool)
    .await
    .map_err(|e| {
        tracing::error!("failed to fetch user: {:?}", e);
        AppError::InternalServerError
    })?
    .ok_or_else(|| AppError::BadRequest("wrong phone number or password".into()))?;

    let is_valid = verify(&req.password, &user.password).map_err(|e| {
        tracing::error!("failed to verify password: {:?}", e);
        AppError::InternalServerError
    })?;

    if !is_valid {
        return Err(AppError::BadRequest(
            "wrong phone number or password".into(),
        ));
    }

    let access_token =
        jwt_provider.generate_access_token(&user.uuid, &user.name, &user.phone_number)?;
    let refresh_token =
        jwt_provider.generate_refresh_token(&user.uuid, &user.name, &user.phone_number)?;

    let ttl = (state.jwt.refresh_expiration / 1000) as u64;
    repo.save_refresh_token(&user.uuid, &refresh_token, ttl)
        .await
        .map_err(|e| {
            tracing::error!("failed to save refresh token: {:?}", e);
            AppError::InternalServerError
        })?;

    Ok(TokenResponse {
        access_token,
        refresh_token,
    })
}
