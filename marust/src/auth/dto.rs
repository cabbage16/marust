use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
pub struct LogInRequest {
    pub phone_number: String,
    pub password: String,
}

#[derive(Serialize)]
pub struct TokenResponse {
    pub access_token: String,
    pub refresh_token: String,
}
