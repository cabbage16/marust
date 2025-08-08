mod common;

use axum::{routing::get, Router};
use common::ApiResponse;

async fn health_check() -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

#[tokio::main]
async fn main() {
    let app = Router::new()
        .route("/health", get(health_check));

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();
    axum::serve(listener, app).await.unwrap();
}
