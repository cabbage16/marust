package com.bamdoliro.maru.shared.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseCleaner {

    private final List<String> tableNameList = new ArrayList<>();

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    @SuppressWarnings("unchecked")
    private void findDatabaseTableNames() {
        List<Object[]> tableInfoList = entityManager.createNativeQuery("SHOW TABLES").getResultList();
        for (Object[] tableInfo : tableInfoList) {
            String tableName = (String) tableInfo[0];
            tableNameList.add(tableName);
        }
    }

    private void truncate() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        for (String tableName : tableNameList) {
            entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s RESTART IDENTITY", tableName)).executeUpdate();
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    @Transactional
    public void clear() {
        entityManager.clear();
        truncate();
    }
}
