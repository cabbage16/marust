use axum::{async_trait, extract::FromRequestParts, http::request::Parts};

use crate::{
    AppState, common::AppError, infrastructure::auth::jwt_provider::JwtProvider,
    user::authority::Authority,
};
use std::str::FromStr;

pub struct AuthUser {
    pub uuid: uuid::Uuid,
    pub authority: Authority,
}

#[async_trait]
impl FromRequestParts<AppState> for AuthUser {
    type Rejection = AppError;

    async fn from_request_parts(
        parts: &mut Parts,
        state: &AppState,
    ) -> Result<Self, Self::Rejection> {
        let auth_header = parts
            .headers
            .get(axum::http::header::AUTHORIZATION)
            .and_then(|v| v.to_str().ok())
            .ok_or_else(|| AppError::BadRequest("missing Authorization header".into()))?;
        let token = auth_header
            .strip_prefix("Bearer ")
            .ok_or_else(|| AppError::BadRequest("invalid Authorization header".into()))?;

        let jwt_provider = JwtProvider::new(
            state.jwt.secret_key.clone(),
            state.jwt.access_expiration,
            state.jwt.refresh_expiration,
        );
        let claims = jwt_provider.parse(token)?;
        if claims.token_type != "ACCESS_TOKEN" {
            return Err(AppError::BadRequest("invalid token".into()));
        }
        let uuid = uuid::Uuid::parse_str(&claims.sub)
            .map_err(|_| AppError::BadRequest("invalid token".into()))?;
        let authority = Authority::from_str(&claims.authority)
            .map_err(|_| AppError::BadRequest("invalid token".into()))?;

        Ok(Self { uuid, authority })
    }
}
