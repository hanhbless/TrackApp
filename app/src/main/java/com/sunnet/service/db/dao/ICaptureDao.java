package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.CaptureEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ICaptureDao {
    List<CaptureEntity> getAll() throws SQLException;

    void createAll(List<CaptureEntity> list) throws SQLException;

    void createOrUpdateEntity(CaptureEntity entity) throws SQLException;

    void deleteEntity(CaptureEntity entity) throws SQLException;
}
