# syntax=docker/dockerfile:1

FROM rust:1.89.0 AS builder
WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y pkg-config libssl-dev libpq-dev && rm -rf /var/lib/apt/lists/*

# Copy full workspace
COPY . .

# Build the question service
RUN cargo build --package question-service --release

FROM debian:bookworm-slim AS runtime
WORKDIR /app
RUN apt-get update && apt-get install -y libssl3 libpq5 ca-certificates && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/release/question-service /usr/local/bin/question-service

CMD ["question-service"]
