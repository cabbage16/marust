use serde::{Deserialize, Serialize};
use std::{fmt, str::FromStr};

/// 사용자 권한을 나타내는 열거형입니다.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum Authority {
    User,
    Admin,
}

#[allow(dead_code)]
impl Authority {
    /// 각 권한에 대한 설명을 반환합니다.
    pub fn description(&self) -> &'static str {
        match self {
            Authority::User => "일반 사용자",
            Authority::Admin => "어드민",
        }
    }
}

impl fmt::Display for Authority {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(match self {
            Authority::User => "USER",
            Authority::Admin => "ADMIN",
        })
    }
}

impl FromStr for Authority {
    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "USER" => Ok(Authority::User),
            "ADMIN" => Ok(Authority::Admin),
            _ => Err(()),
        }
    }
}
