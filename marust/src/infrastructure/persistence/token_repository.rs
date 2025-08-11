use async_trait::async_trait;
use deadpool_redis::{Pool as RedisPool, redis::AsyncCommands};
use uuid::Uuid;

use crate::auth::repository::TokenRepository;

pub struct RedisTokenRepository {
    pool: RedisPool,
}

impl RedisTokenRepository {
    pub fn new(pool: RedisPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl TokenRepository for RedisTokenRepository {
    async fn save_refresh_token(
        &self,
        uuid: &Uuid,
        token: &str,
        ttl: u64,
    ) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
        let mut conn = self.pool.get().await?;
        let _: () = conn.set_ex(uuid.to_string(), token, ttl).await?;
        Ok(())
    }
}
