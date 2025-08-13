use async_trait::async_trait;
use chrono::NaiveDateTime;
use sqlx::{PgPool, Postgres, Transaction};

pub struct NoticeSimpleModel {
    pub id: i64,
    pub title: String,
    pub created_at: NaiveDateTime,
}

pub struct NoticeDetailModel {
    pub id: i64,
    pub title: String,
    pub content: String,
    pub file_names: Vec<String>,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

#[async_trait]
pub trait NoticeRepository {
    async fn insert(
        &self,
        title: &str,
        content: &str,
        file_names: &[String],
    ) -> Result<i64, sqlx::Error>;

    async fn update(
        &self,
        id: i64,
        title: &str,
        content: &str,
        file_names: &[String],
    ) -> Result<Option<Vec<String>>, sqlx::Error>;

    async fn find_all(&self) -> Result<Vec<NoticeSimpleModel>, sqlx::Error>;

    async fn find_by_id(&self, id: i64) -> Result<Option<NoticeDetailModel>, sqlx::Error>;

    async fn delete(&self, id: i64) -> Result<Option<Vec<String>>, sqlx::Error>;
}

pub struct SqlxNoticeRepository {
    pool: PgPool,
}

impl SqlxNoticeRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl NoticeRepository for SqlxNoticeRepository {
    async fn insert(
        &self,
        title: &str,
        content: &str,
        file_names: &[String],
    ) -> Result<i64, sqlx::Error> {
        let mut tx: Transaction<'_, Postgres> = self.pool.begin().await?;
        let id: (i64,) = sqlx::query_as(
            r#"INSERT INTO tbl_notice (title, content, created_at, updated_at)
               VALUES ($1, $2, NOW(), NOW())
               RETURNING notice_id"#,
        )
        .bind(title)
        .bind(content)
        .fetch_one(&mut *tx)
        .await?;

        for name in file_names {
            sqlx::query("INSERT INTO tbl_notice_file (notice_id, file_name) VALUES ($1, $2)")
                .bind(id.0)
                .bind(name)
                .execute(&mut *tx)
                .await?;
        }
        tx.commit().await?;
        Ok(id.0)
    }

    async fn update(
        &self,
        id: i64,
        title: &str,
        content: &str,
        file_names: &[String],
    ) -> Result<Option<Vec<String>>, sqlx::Error> {
        let mut tx: Transaction<'_, Postgres> = self.pool.begin().await?;
        let existing: Option<(i64,)> =
            sqlx::query_as("SELECT notice_id FROM tbl_notice WHERE notice_id = $1")
                .bind(id)
                .fetch_optional(&mut *tx)
                .await?;
        if existing.is_none() {
            tx.rollback().await?;
            return Ok(None);
        }

        let old_files: Vec<String> =
            sqlx::query_scalar("SELECT file_name FROM tbl_notice_file WHERE notice_id = $1")
                .bind(id)
                .fetch_all(&mut *tx)
                .await?;

        sqlx::query(
            r#"UPDATE tbl_notice
               SET title = $1, content = $2, updated_at = NOW()
               WHERE notice_id = $3"#,
        )
        .bind(title)
        .bind(content)
        .bind(id)
        .execute(&mut *tx)
        .await?;

        sqlx::query("DELETE FROM tbl_notice_file WHERE notice_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        for name in file_names {
            sqlx::query("INSERT INTO tbl_notice_file (notice_id, file_name) VALUES ($1, $2)")
                .bind(id)
                .bind(name)
                .execute(&mut *tx)
                .await?;
        }
        tx.commit().await?;
        Ok(Some(old_files))
    }

    async fn find_all(&self) -> Result<Vec<NoticeSimpleModel>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct Row {
            notice_id: i64,
            title: String,
            created_at: NaiveDateTime,
        }
        let rows = sqlx::query_as::<_, Row>(
            r#"SELECT notice_id, title, created_at FROM tbl_notice ORDER BY created_at DESC"#,
        )
        .fetch_all(&self.pool)
        .await?;

        Ok(rows
            .into_iter()
            .map(|r| NoticeSimpleModel {
                id: r.notice_id,
                title: r.title,
                created_at: r.created_at,
            })
            .collect())
    }

    async fn find_by_id(&self, id: i64) -> Result<Option<NoticeDetailModel>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct Row {
            notice_id: i64,
            title: String,
            content: String,
            created_at: NaiveDateTime,
            updated_at: NaiveDateTime,
        }
        let notice = sqlx::query_as::<_, Row>(
            r#"SELECT notice_id, title, content, created_at, updated_at
               FROM tbl_notice WHERE notice_id = $1"#,
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await?;
        let Some(n) = notice else { return Ok(None) };
        let file_names: Vec<String> =
            sqlx::query_scalar("SELECT file_name FROM tbl_notice_file WHERE notice_id = $1")
                .bind(id)
                .fetch_all(&self.pool)
                .await?;
        Ok(Some(NoticeDetailModel {
            id: n.notice_id,
            title: n.title,
            content: n.content,
            file_names,
            created_at: n.created_at,
            updated_at: n.updated_at,
        }))
    }

    async fn delete(&self, id: i64) -> Result<Option<Vec<String>>, sqlx::Error> {
        let mut tx: Transaction<'_, Postgres> = self.pool.begin().await?;
        let file_names: Vec<String> =
            sqlx::query_scalar("SELECT file_name FROM tbl_notice_file WHERE notice_id = $1")
                .bind(id)
                .fetch_all(&mut *tx)
                .await?;

        let result = sqlx::query("DELETE FROM tbl_notice WHERE notice_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        if result.rows_affected() == 0 {
            tx.rollback().await?;
            return Ok(None);
        }
        tx.commit().await?;
        Ok(Some(file_names))
    }
}
