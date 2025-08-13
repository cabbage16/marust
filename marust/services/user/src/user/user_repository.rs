use async_trait::async_trait;
use uuid::Uuid;
use sqlx::PgPool;

use common::Authority;

pub struct UserModel {
    pub phone_number: String,
    pub name: String,
    pub authority: String,
}

#[async_trait]
pub trait UserRepository {
    async fn exists_by_phone(&self, phone_number: &str) -> Result<bool, sqlx::Error>;

    async fn insert_user(
        &self,
        uuid: Uuid,
        phone_number: &str,
        name: &str,
        password_hash: &str,
        authority: Authority,
    ) -> Result<(), sqlx::Error>;

    async fn find_by_uuid(&self, uuid: Uuid) -> Result<Option<UserModel>, sqlx::Error>;
}

pub struct SqlxUserRepository {
    pool: PgPool,
}

impl SqlxUserRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl UserRepository for SqlxUserRepository {
    async fn exists_by_phone(&self, phone_number: &str) -> Result<bool, sqlx::Error> {
        let exists: bool = sqlx::query_scalar::<_, bool>(
            "SELECT EXISTS(SELECT 1 FROM tbl_user WHERE phone_number = $1)",
        )
        .bind(phone_number)
        .fetch_one(&self.pool)
        .await?;

        Ok(exists)
    }

    async fn insert_user(
        &self,
        uuid: Uuid,
        phone_number: &str,
        name: &str,
        password_hash: &str,
        authority: Authority,
    ) -> Result<(), sqlx::Error> {
        sqlx::query(
            r#"INSERT INTO tbl_user (uuid, phone_number, name, password, authority, created_at, updated_at)
               VALUES ($1, $2, $3, $4, $5, NOW(), NOW())"#,
        )
        .bind(uuid)
        .bind(phone_number)
        .bind(name)
        .bind(password_hash)
        .bind(authority.to_string())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    async fn find_by_uuid(&self, uuid: Uuid) -> Result<Option<UserModel>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct Row {
            phone_number: String,
            name: String,
            authority: String,
        }

        let row = sqlx::query_as::<_, Row>(
            "SELECT phone_number, name, authority FROM tbl_user WHERE uuid = $1",
        )
        .bind(uuid)
        .fetch_optional(&self.pool)
        .await?;

        Ok(row.map(|r| UserModel {
            phone_number: r.phone_number,
            name: r.name,
            authority: r.authority,
        }))
    }
}
