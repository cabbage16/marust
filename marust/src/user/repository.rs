use async_trait::async_trait;
use uuid::Uuid;

use super::authority::Authority;

/// 저장소에서 조회된 사용자 정보를 표현합니다.
pub struct UserModel {
    pub phone_number: String,
    pub name: String,
    pub authority: String,
}

/// 사용자 도메인에서 사용할 저장소 추상화
#[async_trait]
pub trait UserRepository {
    /// 주어진 전화번호를 가진 사용자가 존재하는지 확인합니다.
    async fn exists_by_phone(&self, phone_number: &str) -> Result<bool, sqlx::Error>;

    /// 새로운 사용자를 저장합니다.
    async fn insert_user(
        &self,
        uuid: Uuid,
        phone_number: &str,
        name: &str,
        password_hash: &str,
        authority: Authority,
    ) -> Result<(), sqlx::Error>;

    /// uuid로 사용자를 조회합니다.
    async fn find_by_uuid(&self, uuid: Uuid) -> Result<Option<UserModel>, sqlx::Error>;
}
