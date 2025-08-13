use async_trait::async_trait;
use chrono::NaiveDateTime;
use sqlx::PgPool;

use super::question_dto::QuestionCategory;

pub struct QuestionModel {
    pub id: i64,
    pub title: String,
    pub content: String,
    pub category: String,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

#[async_trait]
pub trait QuestionRepository {
    async fn insert(
        &self,
        title: &str,
        content: &str,
        category: QuestionCategory,
    ) -> Result<i64, sqlx::Error>;

    async fn update(
        &self,
        id: i64,
        title: &str,
        content: &str,
        category: QuestionCategory,
    ) -> Result<bool, sqlx::Error>;

    async fn find_by_category(
        &self,
        category: Option<QuestionCategory>,
    ) -> Result<Vec<QuestionModel>, sqlx::Error>;

    async fn find_by_id(&self, id: i64) -> Result<Option<QuestionModel>, sqlx::Error>;

    async fn delete(&self, id: i64) -> Result<bool, sqlx::Error>;
}

pub struct SqlxQuestionRepository {
    pool: PgPool,
}

impl SqlxQuestionRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl QuestionRepository for SqlxQuestionRepository {
    async fn insert(
        &self,
        title: &str,
        content: &str,
        category: QuestionCategory,
    ) -> Result<i64, sqlx::Error> {
        let id: (i64,) = sqlx::query_as(
            r#"INSERT INTO tbl_question (title, content, category, created_at, updated_at)
               VALUES ($1, $2, $3, NOW(), NOW())
               RETURNING question_id"#,
        )
        .bind(title)
        .bind(content)
        .bind(category.to_string())
        .fetch_one(&self.pool)
        .await?;
        Ok(id.0)
    }

    async fn update(
        &self,
        id: i64,
        title: &str,
        content: &str,
        category: QuestionCategory,
    ) -> Result<bool, sqlx::Error> {
        let result = sqlx::query(
            r#"UPDATE tbl_question
               SET title = $1, content = $2, category = $3, updated_at = NOW()
               WHERE question_id = $4"#,
        )
        .bind(title)
        .bind(content)
        .bind(category.to_string())
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(result.rows_affected() > 0)
    }

    async fn find_by_category(
        &self,
        category: Option<QuestionCategory>,
    ) -> Result<Vec<QuestionModel>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct Row {
            question_id: i64,
            title: String,
            content: String,
            category: String,
            created_at: NaiveDateTime,
            updated_at: NaiveDateTime,
        }

        let rows = match category {
            Some(c) => {
                sqlx::query_as::<_, Row>(
                    r#"SELECT question_id, title, content, category, created_at, updated_at
                       FROM tbl_question
                       WHERE category = $1
                       ORDER BY question_id DESC"#,
                )
                .bind(c.to_string())
                .fetch_all(&self.pool)
                .await?
            }
            None => {
                sqlx::query_as::<_, Row>(
                    r#"SELECT question_id, title, content, category, created_at, updated_at
                       FROM tbl_question
                       ORDER BY question_id DESC"#,
                )
                .fetch_all(&self.pool)
                .await?
            }
        };

        Ok(rows
            .into_iter()
            .map(|r| QuestionModel {
                id: r.question_id,
                title: r.title,
                content: r.content,
                category: r.category,
                created_at: r.created_at,
                updated_at: r.updated_at,
            })
            .collect())
    }

    async fn find_by_id(&self, id: i64) -> Result<Option<QuestionModel>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct Row {
            question_id: i64,
            title: String,
            content: String,
            category: String,
            created_at: NaiveDateTime,
            updated_at: NaiveDateTime,
        }

        let row = sqlx::query_as::<_, Row>(
            r#"SELECT question_id, title, content, category, created_at, updated_at
               FROM tbl_question
               WHERE question_id = $1"#,
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await?;

        Ok(row.map(|r| QuestionModel {
            id: r.question_id,
            title: r.title,
            content: r.content,
            category: r.category,
            created_at: r.created_at,
            updated_at: r.updated_at,
        }))
    }

    async fn delete(&self, id: i64) -> Result<bool, sqlx::Error> {
        let result = sqlx::query("DELETE FROM tbl_question WHERE question_id = $1")
            .bind(id)
            .execute(&self.pool)
            .await?;
        Ok(result.rows_affected() > 0)
    }
}
