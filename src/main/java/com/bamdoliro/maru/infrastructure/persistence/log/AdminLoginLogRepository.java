package com.bamdoliro.maru.infrastructure.persistence.log;

import com.bamdoliro.maru.domain.log.AdminLoginLog;
import org.springframework.data.repository.CrudRepository;

public interface AdminLoginLogRepository extends CrudRepository<AdminLoginLog, Long> {
}
