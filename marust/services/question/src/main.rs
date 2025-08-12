mod question;

use axum::{
    extract::{FromRef, State},
    routing::get,
    Router,
};
use common::ApiResponse;
use serde::Deserialize;
use sqlx::postgres::{PgConnectOptions, PgPool, PgPoolOptions};
use std::net::SocketAddr;

use dotenvy::dotenv;
use infrastructure::auth::jwt_provider::JwtProvider;

async fn health_check(State(_): State<AppState>) -> ApiResponse<&'static str> {
    ApiResponse::ok("ok")
}

#[derive(Clone)]
pub struct AppState {
    pub pg_pool: PgPool,
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

    jwt_secret_key: String,

    question_port: u16,
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
        access_expiration: 3600000,     // 1시간
        refresh_expiration: 1296000000, // 15일
    };

    let state = AppState { pg_pool, jwt };

    let app = Router::new()
        .route("/health", get(health_check))
        .merge(question::router())
        .with_state(state);

    let addr: SocketAddr = ([0, 0, 0, 0], cfg.question_port).into();
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
