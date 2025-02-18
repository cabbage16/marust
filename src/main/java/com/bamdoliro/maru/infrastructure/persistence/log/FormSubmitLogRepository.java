package com.bamdoliro.maru.infrastructure.persistence.log;

import com.bamdoliro.maru.domain.log.FormSubmitLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmitLogRepository extends JpaRepository<FormSubmitLog, Long> {
}
