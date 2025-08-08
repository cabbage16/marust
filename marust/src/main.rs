mod common;

use axum::{Router, routing::get};
use common::{ApiResponse, AppError};

async fn health_check() -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

async fn always_fail() -> Result<ApiResponse<&'static str>, AppError> {
    Err(AppError::InternalServerError)
}

#[tokio::main]
async fn main() {
    let app = Router::new()
        .route("/health", get(health_check))
        .route("/fail", get(always_fail));

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();
    axum::serve(listener, app).await.unwrap();
}
