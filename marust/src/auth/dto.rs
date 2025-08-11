use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
pub struct LogInRequest {
    pub phone_number: String,
    pub password: String,
}

#[derive(Serialize)]
pub struct TokenResponse {
    pub access_token: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub refresh_token: Option<String>,
}
