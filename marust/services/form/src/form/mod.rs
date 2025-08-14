pub mod form_dto;
mod form_handler;
mod form_service;
mod form_repository;

use axum::Router;
use axum::routing::post;

use crate::AppState;

pub fn router() -> Router<AppState> {
    Router::new().route("/forms", post(form_handler::submit_form))
}
