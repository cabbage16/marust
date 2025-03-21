package com.bamdoliro.maru.infrastructure.persistence.log;

import com.bamdoliro.maru.domain.log.FormUpdateLog;
import org.springframework.data.repository.CrudRepository;

public interface FormUpdateLogRepository extends CrudRepository<FormUpdateLog, Long> {
}
