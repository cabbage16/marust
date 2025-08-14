pub mod form_dto;
mod form_handler;
mod form_service;
mod form_repository;

use axum::Router;
use axum::routing::{get, post};

use crate::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        .route(
            "/forms",
            post(form_handler::submit_form).get(form_handler::get_form_list),
        )
        .route("/forms/me", get(form_handler::get_my_form))
        .route("/forms/:id", get(form_handler::get_form))
}