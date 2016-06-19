package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.CallVoiceEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ICallVoiceDao {
    List<CallVoiceEntity> getAll() throws SQLException;

    void createAll(List<CallVoiceEntity> list) throws SQLException;

    void createOrUpdateEntity(CallVoiceEntity entity) throws SQLException;

    void deleteEntity(CallVoiceEntity entity) throws SQLException;

    void deleteAll(List<CallVoiceEntity> list) throws SQLException;
}
