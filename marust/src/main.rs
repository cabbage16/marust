// src/main.rs
mod auth;
mod common;
mod infrastructure;
mod user;

use axum::{
    Router,
    extract::State,
    routing::get,
};
use common::ApiResponse;
use deadpool_redis::{Config as RedisConfig, Pool as RedisPool, Runtime};
use serde::Deserialize;
use sqlx::postgres::{PgConnectOptions, PgPool, PgPoolOptions};
use std::net::SocketAddr;

use dotenvy::dotenv;

async fn health_check(State(_): State<AppState>) -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

#[derive(Clone)]
pub struct AppState {
    pub pg_pool: PgPool,
    pub redis_pool: RedisPool,
    pub jwt: JwtConfig,
}

#[derive(Clone)]
pub struct JwtConfig {
    pub secret_key: String,
    pub access_expiration: i64,
    pub refresh_expiration: i64,
}

#[derive(Deserialize, Debug)]
struct Settings {
    db_host: String,
    db_port: u16,
    db_username: String,
    db_password: String,
    db_name: String,

    redis_host: String,
    redis_port: u16,
    redis_password: String,

    jwt_secret_key: String,

    port: u16,
}

fn load_settings() -> Settings {
    dotenv().ok(); // .env 있으면 읽어옵니다 (없어도 무시)
    // 필드명 기반으로 환경변수 매핑 (예: db_host -> DB_HOST)
    envy::from_env::<Settings>().expect("failed to load environment variables into Settings")
}

#[tokio::main]
async fn main() {
    let cfg = load_settings();

    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::DEBUG)
        .init();

    let pg_options = PgConnectOptions::new()
        .host(&cfg.db_host)
        .port(cfg.db_port)
        .username(&cfg.db_username)
        .password(&cfg.db_password)
        .database(&cfg.db_name);

    let pg_pool = PgPoolOptions::new()
        .connect_with(pg_options)
        .await
        .expect("failed to connect to PostgreSQL");

    let redis_url = format!(
        "redis://:{}@{}:{}/",
        cfg.redis_password, cfg.redis_host, cfg.redis_port
    );
    let redis_cfg = RedisConfig::from_url(redis_url);
    let redis_pool = redis_cfg
        .create_pool(Some(Runtime::Tokio1))
        .expect("failed to create Redis pool");

    let jwt = JwtConfig {
        secret_key: cfg.jwt_secret_key.clone(),
        access_expiration: 3600000, // 1시간
        refresh_expiration: 1296000000, // 15일
    };

    let state = AppState {
        pg_pool,
        redis_pool,
        jwt,
    };

    let app = Router::new()
        .route("/health", get(health_check))
        .merge(auth::router())
        .merge(user::router())
        .with_state(state);

    let addr: SocketAddr = ([0, 0, 0, 0], cfg.port).into();
    let listener = tokio::net::TcpListener::bind(addr)
        .await
        .expect("failed to bind listener");
    axum::serve(listener, app).await.expect("server error");
}
