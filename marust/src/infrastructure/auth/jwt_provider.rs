use chrono::{Duration, Utc};
use jsonwebtoken::{decode, encode, DecodingKey, EncodingKey, Header, Validation};

use crate::common::AppError;

#[derive(Clone)]
pub struct JwtProvider {
    secret: String,
    access_exp: i64,
    refresh_exp: i64,
}

impl JwtProvider {
    pub fn new(secret: String, access_exp: i64, refresh_exp: i64) -> Self {
        Self {
            secret,
            access_exp,
            refresh_exp,
        }
    }

    fn generate_token(
        &self,
        uuid: &uuid::Uuid,
        name: &str,
        phone_number: &str,
        token_type: &str,
        exp_ms: i64,
    ) -> Result<String, AppError> {
        #[derive(serde::Serialize)]
        struct Claims<'a> {
            sub: &'a str,
            iss: &'static str,
            name: &'a str,
            phone_number: &'a str,
            #[serde(rename = "type")]
            token_type: &'a str,
            exp: usize,
            iat: usize,
        }

        let now = Utc::now();
        let exp = now + Duration::milliseconds(exp_ms);
        let claims = Claims {
            sub: &uuid.to_string(),
            iss: "marust",
            name,
            phone_number,
            token_type,
            iat: now.timestamp() as usize,
            exp: exp.timestamp() as usize,
        };

        encode(
            &Header::default(),
            &claims,
            &EncodingKey::from_secret(self.secret.as_bytes()),
        )
        .map_err(|e| {
            tracing::error!("failed to generate token: {:?}", e);
            AppError::InternalServerError
        })
    }

    pub fn generate_access_token(
        &self,
        uuid: &uuid::Uuid,
        name: &str,
        phone_number: &str,
    ) -> Result<String, AppError> {
        self.generate_token(uuid, name, phone_number, "ACCESS_TOKEN", self.access_exp)
    }

    pub fn generate_refresh_token(
        &self,
        uuid: &uuid::Uuid,
        name: &str,
        phone_number: &str,
    ) -> Result<String, AppError> {
        self.generate_token(uuid, name, phone_number, "REFRESH_TOKEN", self.refresh_exp)
    }

    pub fn parse(&self, token: &str) -> Result<Claims, AppError> {
        decode::<Claims>(
            token,
            &DecodingKey::from_secret(self.secret.as_bytes()),
            &Validation::default(),
        )
        .map(|data| data.claims)
        .map_err(|e| {
            tracing::error!("failed to decode token: {:?}", e);
            AppError::BadRequest("invalid token".into())
        })
    }
}

#[derive(serde::Deserialize)]
pub struct Claims {
    pub sub: String,
    pub name: String,
    pub phone_number: String,
    #[serde(rename = "type")]
    pub token_type: String,
}
