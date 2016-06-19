package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.util.Utils;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class LocationDao extends BaseDaoImpl<LocationEntity, String> implements ILocationDao {
    public LocationDao(Class<LocationEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public LocationDao(ConnectionSource connectionSource, Class<LocationEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public LocationDao(ConnectionSource connectionSource, DatabaseTableConfig<LocationEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<LocationEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (LocationEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + LocationEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + LocationEntity.ID_COL + "', ");
        builder.append("'" + LocationEntity.DATE_COL + "', ");
        builder.append("'" + LocationEntity.LAT_COL + "', ");
        builder.append("'" + LocationEntity.LONG_COL + "', ");
        builder.append("'" + LocationEntity.PLACE_COL + "', ");
        builder.append("'" + LocationEntity.ADDRESS_COL + "'");
        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            LocationEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getLatitude() + "', ");
            builder.append("'" + item.getLongitude() + "', ");
            builder.append("'" + item.getPlace() + "', ");
            builder.append("'" + item.getAddress() + "'");
            builder.append(")");

            if (i < count - 1)
                builder.append(",");
        }
        builder.append(";");

        //-- Execute sql
        executeRawNoArgs(builder.toString());
    }

    @Override
    public void createOrUpdateEntity(LocationEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public List<LocationEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public LocationEntity getLastEntity() throws SQLException {
        QueryBuilder<LocationEntity, String> builder = queryBuilder();
        builder.limit(1);
        builder.orderBy(LocationEntity.DATE_COL, false);  // true for ascending, false for descending
        List<LocationEntity> list = query(builder.prepare());  // returns list of ten items
        if (list != null && list.size() > 0)
            return list.get(0);
        return null;
    }

    @Override
    public void deleteEntity(LocationEntity entity) throws SQLException {
        delete(entity);
    }

    @Override
    public void deleteAll(List<LocationEntity> list) throws SQLException {
        String userStr = Utils.getStringLocationId(list);

        if (Utils.isEmptyString(userStr))
            return;

        DeleteBuilder<LocationEntity, String> deleteBuilder = deleteBuilder();
        // Remove 'quote' in the first and bottom of String Because of [Where In Clause in Ormlite format: "('" + value_1' + 'value_2' + ... 'value_n + "')" ]
        userStr = userStr.substring(1, userStr.length() - 1);
        deleteBuilder.where().in(LocationEntity.ID_COL, userStr);
        deleteBuilder.delete();
    }
}
