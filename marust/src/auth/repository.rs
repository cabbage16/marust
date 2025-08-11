use async_trait::async_trait;
use uuid::Uuid;

#[async_trait]
pub trait TokenRepository {
    async fn save_refresh_token(
        &self,
        uuid: &Uuid,
        token: &str,
        ttl: u64,
    ) -> Result<(), Box<dyn std::error::Error + Send + Sync>>;

    async fn find_refresh_token(
        &self,
        uuid: &Uuid,
    ) -> Result<Option<String>, Box<dyn std::error::Error + Send + Sync>>;

    async fn delete_refresh_token(
        &self,
        uuid: &Uuid,
    ) -> Result<(), Box<dyn std::error::Error + Send + Sync>>;
}
