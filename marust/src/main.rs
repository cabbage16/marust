mod common;

use axum::{routing::get, Router};
use common::ApiResponse;
use deadpool_redis::{Config as RedisConfig, Pool as RedisPool, Runtime};
use sqlx::postgres::{PgConnectOptions, PgPool, PgPoolOptions};
use std::env;

async fn health_check() -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

#[derive(Clone)]
struct AppState {
    pg_pool: PgPool,
    redis_pool: RedisPool,
}

#[tokio::main]
async fn main() {
    let db_host = env::var("DB_HOST").expect("DB_HOST must be set");
    let db_port: u16 = env::var("DB_PORT")
        .expect("DB_PORT must be set")
        .parse()
        .expect("DB_PORT must be a number");
    let db_user = env::var("DB_USERNAME").expect("DB_USERNAME must be set");
    let db_password = env::var("DB_PASSWORD").expect("DB_PASSWORD must be set");
    let db_name = env::var("DB_NAME").expect("DB_NAME must be set");

    let pg_options = PgConnectOptions::new()
        .host(&db_host)
        .port(db_port)
        .username(&db_user)
        .password(&db_password)
        .database(&db_name);

    let pg_pool = PgPoolOptions::new()
        .connect_with(pg_options)
        .await
        .expect("failed to connect to PostgreSQL");

    let redis_host = env::var("REDIS_HOST").expect("REDIS_HOST must be set");
    let redis_port: u16 = env::var("REDIS_PORT")
        .expect("REDIS_PORT must be set")
        .parse()
        .expect("REDIS_PORT must be a number");
    let redis_password = env::var("REDIS_PASSWORD").expect("REDIS_PASSWORD must be set");

    let redis_url = format!("redis://:{}@{}:{}/", redis_password, redis_host, redis_port);
    let redis_cfg = RedisConfig::from_url(redis_url);
    let redis_pool = redis_cfg
        .create_pool(Some(Runtime::Tokio1))
        .expect("failed to create Redis pool");

    let state = AppState {
        pg_pool,
        redis_pool,
    };

    let app = Router::new()
        .route("/health", get(health_check))
        .with_state(state);

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000")
        .await
        .expect("failed to bind listener");
    axum::serve(listener, app).await.expect("server error");
}