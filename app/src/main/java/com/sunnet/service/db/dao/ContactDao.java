package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.ContactEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class ContactDao extends BaseDaoImpl<ContactEntity, String> implements IContactDao {
    public ContactDao(Class<ContactEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public ContactDao(ConnectionSource connectionSource, Class<ContactEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public ContactDao(ConnectionSource connectionSource, DatabaseTableConfig<ContactEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<ContactEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (ContactEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + ContactEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + ContactEntity.ID_COL + "', ");
        builder.append("'" + ContactEntity.DATE_COL + "', ");
        builder.append("'" + ContactEntity.NAME_COL + "', ");
        builder.append("'" + ContactEntity.PHONE_COL + "', ");
        builder.append("'" + ContactEntity.STATUS_COL + "'");
        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            ContactEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getName() + "', ");
            builder.append("'" + item.getPhone() + "', ");
            builder.append(item.getStatus());
            builder.append(")");

            if (i < count - 1)
                builder.append(",");
        }
        builder.append(";");

        //-- Execute sql
        executeRawNoArgs(builder.toString());
    }

    @Override
    public void createOrUpdateEntity(ContactEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public List<ContactEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public void updateAll(List<ContactEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (ContactEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        int count = list.size();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE '" + ContactEntity.TABLE_NAME + "'");
        stringBuilder.append(" SET ");

        //-- STATUS_COL
        stringBuilder.append("'" + ContactEntity.STATUS_COL + "'");
        stringBuilder.append(" = CASE " + ContactEntity.ID_COL);
        for (int i = 0; i < count; i++) {
            ContactEntity item = list.get(i);
            stringBuilder.append(" WHEN '" + item.getId() + "'");
            stringBuilder.append(" THEN " + item.getStatus());
        }
        stringBuilder.append(" END");
        // WHERE CLAUSE
        stringBuilder.append(" WHERE " + ContactEntity.ID_COL + " IN (");
        //int count = listItems.size();
        for (int j = 0; j < count; j++) {
            ContactEntity item = list.get(j);
            stringBuilder.append("'" + item.getId() + "'");
            if (j < count - 1)
                stringBuilder.append(",");
        }
        stringBuilder.append(")");
        stringBuilder.append(";");

        //-- Execute sql
        executeRawNoArgs(stringBuilder.toString());
    }

    @Override
    public void deleteEntity(ContactEntity entity) throws SQLException {
        delete(entity);
    }

    @Override
    public void deleteAll(List<ContactEntity> list) throws SQLException {
    }
}
