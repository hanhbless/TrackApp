package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.CaptureEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class CaptureDao extends BaseDaoImpl<CaptureEntity, String> implements ICaptureDao {
    public CaptureDao(Class<CaptureEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public CaptureDao(ConnectionSource connectionSource, Class<CaptureEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public CaptureDao(ConnectionSource connectionSource, DatabaseTableConfig<CaptureEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<CaptureEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (CaptureEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + CaptureEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + CaptureEntity.ID_COL + "', ");
        builder.append("'" + CaptureEntity.PHONE_COL + "', ");
        builder.append("'" + CaptureEntity.DATE_COL + "', ");
        builder.append("'" + CaptureEntity.PICTURE_COL + "'");
        builder.append("'" + CaptureEntity.PKG_COL + "'");

        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            CaptureEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getPhone() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getPicture() + "'");
            builder.append("'" + item.getTopPackage() + "'");

            builder.append(")");

            if (i < count - 1)
                builder.append(",");
        }
        builder.append(";");

        //-- Execute sql
        executeRawNoArgs(builder.toString());
    }

    @Override
    public void createOrUpdateEntity(CaptureEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public List<CaptureEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public void deleteEntity(CaptureEntity entity) throws SQLException {
        delete(entity);
    }
}
