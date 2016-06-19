package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.SMSEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ISMSDao {
    List<SMSEntity> getAll() throws SQLException;

    List<SMSEntity> getAllByStatus(int status) throws SQLException;

    void createAll(List<SMSEntity> list) throws SQLException;

    void updateAll(List<SMSEntity> list) throws SQLException;

    void createOrUpdateEntity(SMSEntity entity) throws SQLException;

    void deleteEntity(SMSEntity entity) throws SQLException;

    void deleteAll(List<SMSEntity> list) throws SQLException;
}
