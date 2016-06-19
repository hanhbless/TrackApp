package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.ContactEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface IContactDao {
    List<ContactEntity> getAll() throws SQLException;

    void createAll(List<ContactEntity> list) throws SQLException;

    void updateAll(List<ContactEntity> list) throws SQLException;

    void createOrUpdateEntity(ContactEntity entity) throws SQLException;

    void deleteEntity(ContactEntity entity) throws SQLException;

    void deleteAll(List<ContactEntity> list) throws SQLException;
}
