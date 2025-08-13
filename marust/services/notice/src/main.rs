mod notice;

use axum::{
    Router,
    extract::{FromRef, State},
    routing::get,
};
use common::ApiResponse;
use dotenvy::dotenv;
use infrastructure::auth::jwt_provider::JwtProvider;
use serde::Deserialize;
use sqlx::postgres::{PgConnectOptions, PgPool, PgPoolOptions};
use std::net::SocketAddr;
use tower_http::services::ServeDir;

const NOTICE_STORAGE_PATH: &str = "./storage/notice";

async fn health_check(State(_): State<AppState>) -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

#[derive(Clone)]
pub struct AppState {
    pub pg_pool: PgPool,
    pub jwt: JwtConfig,
    pub storage_path: String,
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

    jwt_secret_key: String,

    notice_port: u16,
}

fn load_settings() -> Settings {
    dotenv().ok();
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

    let jwt = JwtConfig {
        secret_key: cfg.jwt_secret_key.clone(),
        access_expiration: 3_600_000,      // 1시간
        refresh_expiration: 1_296_000_000, // 15일
    };

    tokio::fs::create_dir_all(NOTICE_STORAGE_PATH)
        .await
        .expect("failed to create notice storage directory");

    let state = AppState {
        pg_pool,
        jwt,
        storage_path: NOTICE_STORAGE_PATH.to_string(),
    };

    let app = Router::new()
        .route("/health", get(health_check))
        .merge(notice::router())
        .nest_service("/files", ServeDir::new(NOTICE_STORAGE_PATH))
        .with_state(state);

    let addr: SocketAddr = ([0, 0, 0, 0], cfg.notice_port).into();
    let listener = tokio::net::TcpListener::bind(addr)
        .await
        .expect("failed to bind listener");
    axum::serve(listener, app).await.expect("server error");
}

impl FromRef<AppState> for JwtProvider {
    fn from_ref(state: &AppState) -> JwtProvider {
        JwtProvider::new(
            state.jwt.secret_key.clone(),
            state.jwt.access_expiration,
            state.jwt.refresh_expiration,
        )
    }
}
