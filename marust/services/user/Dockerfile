# syntax=docker/dockerfile:1

FROM rust:1.89.0 AS builder
WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y pkg-config libssl-dev libpq-dev && rm -rf /var/lib/apt/lists/*

# Copy full workspace
COPY . .

# Build the user service
RUN cargo build --package user-service --release

FROM debian:bookworm-slim AS runtime
WORKDIR /app
RUN apt-get update && apt-get install -y libssl3 libpq5 ca-certificates && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/release/user-service /usr/local/bin/user-service

CMD ["user-service"]

