package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.CallLogEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ICallLogDao {
    List<CallLogEntity> getAll() throws SQLException;

    void createAll(List<CallLogEntity> list) throws SQLException;

    void createOrUpdateEntity(CallLogEntity entity) throws SQLException;

    void deleteEntity(CallLogEntity entity) throws SQLException;
}
