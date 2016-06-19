package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.CallLogEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class CallLogDao extends BaseDaoImpl<CallLogEntity, String> implements ICallLogDao {
    public CallLogDao(Class<CallLogEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public CallLogDao(ConnectionSource connectionSource, Class<CallLogEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public CallLogDao(ConnectionSource connectionSource, DatabaseTableConfig<CallLogEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<CallLogEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (CallLogEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + CallLogEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + CallLogEntity.ID_COL + "', ");
        builder.append("'" + CallLogEntity.TYPE_COL + "', ");
        builder.append("'" + CallLogEntity.DATE_COL + "', ");
        builder.append("'" + CallLogEntity.PHONE_NUMBER_COL + "', ");
        builder.append("'" + CallLogEntity.PHONE_NAME_COL + "', ");
        builder.append("'" + CallLogEntity.DURATION_COL + "'");
        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            CallLogEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getType() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getPhoneNumber() + "', ");
            builder.append("'" + item.getPhoneName() + "', ");
            builder.append("'" + item.getDuration() + "'");
            builder.append(")");

            if (i < count - 1)
                builder.append(",");
        }
        builder.append(";");

        //-- Execute sql
        executeRawNoArgs(builder.toString());
    }

    @Override
    public void createOrUpdateEntity(CallLogEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public List<CallLogEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public void deleteEntity(CallLogEntity entity) throws SQLException {
        delete(entity);
    }
}
