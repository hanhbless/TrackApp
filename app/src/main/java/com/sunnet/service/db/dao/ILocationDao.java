package com.sunnet.service.db.dao;

import com.sunnet.service.db.entity.LocationEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ILocationDao {

    List<LocationEntity> getAll() throws SQLException;

    void createAll(List<LocationEntity> list) throws SQLException;

    void createOrUpdateEntity(LocationEntity entity) throws SQLException;

    LocationEntity getLastEntity() throws SQLException;

    void deleteEntity(LocationEntity entity) throws SQLException;

    void deleteAll(List<LocationEntity> list) throws SQLException;
}
