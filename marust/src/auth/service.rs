use crate::{
    AppState, auth::repository::TokenRepository, common::AppError,
    infrastructure::auth::jwt_provider::JwtProvider, user::authority::Authority,
};
use bcrypt::verify;
use std::str::FromStr;

use super::dto::{LogInRequest, TokenResponse};

#[derive(sqlx::FromRow)]
struct UserRow {
    uuid: uuid::Uuid,
    password: String,
    authority: String,
}

pub async fn log_in(
    state: &AppState,
    jwt_provider: &JwtProvider,
    repo: &impl TokenRepository,
    req: LogInRequest,
) -> Result<TokenResponse, AppError> {
    let user = sqlx::query_as::<_, UserRow>(
        "SELECT uuid, password, authority FROM tbl_user WHERE phone_number = $1",
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

    let authority =
        Authority::from_str(&user.authority).map_err(|_| AppError::InternalServerError)?;
    let access_token = jwt_provider.generate_access_token(&user.uuid, &authority)?;
    let refresh_token = jwt_provider.generate_refresh_token(&user.uuid, &authority)?;

    let ttl = (state.jwt.refresh_expiration / 1000) as u64;
    repo.save_refresh_token(&user.uuid, &refresh_token, ttl)
        .await
        .map_err(|e| {
            tracing::error!("failed to save refresh token: {:?}", e);
            AppError::InternalServerError
        })?;

    Ok(TokenResponse {
        access_token,
        refresh_token: Some(refresh_token),
    })
}

pub async fn refresh_token(
    jwt_provider: &JwtProvider,
    repo: &impl TokenRepository,
    token: String,
) -> Result<TokenResponse, AppError> {
    let claims = jwt_provider.parse(&token)?;
    if claims.token_type != "REFRESH_TOKEN" {
        return Err(AppError::BadRequest("invalid token".into()));
    }
    let uuid = uuid::Uuid::parse_str(&claims.sub)
        .map_err(|_| AppError::BadRequest("invalid token".into()))?;
    let stored = repo
        .find_refresh_token(&uuid)
        .await
        .map_err(|e| {
            tracing::error!("failed to find refresh token: {:?}", e);
            AppError::InternalServerError
        })?
        .ok_or_else(|| AppError::BadRequest("expired token".into()))?;
    if stored != token {
        return Err(AppError::BadRequest("expired token".into()));
    }
    let authority = Authority::from_str(&claims.authority)
        .map_err(|_| AppError::BadRequest("invalid token".into()))?;
    let access_token = jwt_provider.generate_access_token(&uuid, &authority)?;
    Ok(TokenResponse {
        access_token,
        refresh_token: None,
    })
}

pub async fn log_out(repo: &impl TokenRepository, uuid: uuid::Uuid) -> Result<(), AppError> {
    repo.delete_refresh_token(&uuid).await.map_err(|e| {
        tracing::error!("failed to delete refresh token: {:?}", e);
        AppError::InternalServerError
    })?;
    Ok(())
}
