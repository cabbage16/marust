use sqlx::PgPool;
use uuid::Uuid;

pub async fn exists_by_phone(pool: &PgPool, phone_number: &str) -> Result<bool, sqlx::Error> {
    let exists: bool = sqlx::query_scalar::<_, bool>(
        "SELECT EXISTS(SELECT 1 FROM tbl_user WHERE phone_number = $1)"
    )
        .bind(phone_number)
        .fetch_one(pool)
        .await?;

    Ok(exists)
}

pub async fn insert_user(
    pool: &PgPool,
    uuid: Uuid,
    phone_number: &str,
    name: &str,
    password_hash: &str,
) -> Result<(), sqlx::Error> {
    sqlx::query(
        r#"INSERT INTO tbl_user (uuid, phone_number, name, password, authority, created_at, updated_at)
           VALUES ($1, $2, $3, $4, 'USER', NOW(), NOW())"#,
    )
        .bind(uuid)
        .bind(phone_number)
        .bind(name)
        .bind(password_hash)
        .execute(pool)
        .await?;
    Ok(())
}