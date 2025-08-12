use crate::user::authority::Authority;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
pub struct SignUpUserRequest {
    pub phone_number: String,
    pub name: String,
    pub password: String,
}

#[derive(Serialize)]
pub struct UserResponse {
    pub phone_number: String,
    pub name: String,
    pub authority: Authority,
}
