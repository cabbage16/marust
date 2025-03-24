package com.bamdoliro.maru.infrastructure.persistence.log;

import com.bamdoliro.maru.domain.log.FormSubmitLog;
import org.springframework.data.repository.CrudRepository;

public interface FormSubmitLogRepository extends CrudRepository<FormSubmitLog, Long> {
}
