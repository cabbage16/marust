package com.bamdoliro.maru.infrastructure.persistence.log;

import com.bamdoliro.maru.domain.log.UpdatedField;
import org.springframework.data.repository.CrudRepository;

public interface UpdatedFieldRepository extends CrudRepository<UpdatedField, Long> {
}
