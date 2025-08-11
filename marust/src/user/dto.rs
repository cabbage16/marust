use serde::Deserialize;

#[derive(Deserialize)]
pub struct SignUpUserRequest {
    pub phone_number: String,
    pub name: String,
    pub password: String,
}