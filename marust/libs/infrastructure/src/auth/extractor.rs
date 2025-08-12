use axum::{async_trait, extract::FromRequestParts, http::request::Parts};
use axum::extract::FromRef;

use common::{AppError, Authority};
use std::str::FromStr;

use super::jwt_provider::JwtProvider;

/// 인증된 사용자를 표현하는 추출기입니다.
#[derive(Debug, Clone)]
pub struct AuthUser {
    pub uuid: uuid::Uuid,
    pub authority: Authority,
}

#[async_trait]
impl<S> FromRequestParts<S> for AuthUser
where
    JwtProvider: FromRef<S>,
    S: Send + Sync,
{
    type Rejection = AppError;

    async fn from_request_parts(
        parts: &mut Parts,
        state: &S,
    ) -> Result<Self, Self::Rejection> {
        let auth_header = parts
            .headers
            .get(axum::http::header::AUTHORIZATION)
            .and_then(|v| v.to_str().ok())
            .ok_or_else(|| AppError::BadRequest("missing Authorization header".into()))?;
        let token = auth_header
            .strip_prefix("Bearer ")
            .ok_or_else(|| AppError::BadRequest("invalid Authorization header".into()))?;

        let jwt_provider = JwtProvider::from_ref(state);
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

