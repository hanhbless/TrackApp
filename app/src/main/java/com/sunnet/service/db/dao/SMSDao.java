package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.util.Utils;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class SMSDao extends BaseDaoImpl<SMSEntity, String> implements ISMSDao {
    public SMSDao(Class<SMSEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public SMSDao(ConnectionSource connectionSource, Class<SMSEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public SMSDao(ConnectionSource connectionSource, DatabaseTableConfig<SMSEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<SMSEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (SMSEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + SMSEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + SMSEntity.ID_COL + "', ");
        builder.append("'" + SMSEntity.DATE_COL + "', ");
        builder.append("'" + SMSEntity.SENDER_COL + "', ");
        builder.append("'" + SMSEntity.RECEIVER_COL + "', ");
        builder.append("'" + SMSEntity.BODY_COL + "', ");
        builder.append("'" + SMSEntity.TYPE_COL + "', ");
        builder.append("'" + SMSEntity.STATUS_COL + "'");
        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            SMSEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getSender() + "', ");
            builder.append("'" + item.getReceiver() + "', ");
            builder.append("'" + item.getBody() + "', ");
            builder.append(item.getType() + ",");
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
    public List<SMSEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public List<SMSEntity> getAllByStatus(int status) throws SQLException {
        Where<SMSEntity, String> where = queryBuilder().where();
        where.in(SMSEntity.STATUS_COL, status);
        return where.query();
    }

    @Override
    public void createOrUpdateEntity(SMSEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public void updateAll(List<SMSEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (SMSEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        int count = list.size();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE '" + SMSEntity.TABLE_NAME + "'");
        stringBuilder.append(" SET ");

        //-- STATUS_COL
        stringBuilder.append("'" + SMSEntity.STATUS_COL + "'");
        stringBuilder.append(" = CASE " + SMSEntity.ID_COL);
        for (int i = 0; i < count; i++) {
            SMSEntity item = list.get(i);
            stringBuilder.append(" WHEN '" + item.getId() + "'");
            stringBuilder.append(" THEN " + item.getStatus());
        }
        stringBuilder.append(" END");
        // WHERE CLAUSE
        stringBuilder.append(" WHERE " + SMSEntity.ID_COL + " IN (");
        //int count = listItems.size();
        for (int j = 0; j < count; j++) {
            SMSEntity item = list.get(j);
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
    public void deleteEntity(SMSEntity entity) throws SQLException {
        delete(entity);
    }

    @Override
    public void deleteAll(List<SMSEntity> list) throws SQLException {
        String idList = Utils.getStringSmsId(list);

        if (Utils.isEmptyString(idList))
            return;

        DeleteBuilder<SMSEntity, String> deleteBuilder = deleteBuilder();
        // Remove 'quote' in the first and bottom of String Because of [Where In Clause in Ormlite format: "('" + value_1' + 'value_2' + ... 'value_n + "')" ]
        idList = idList.substring(1, idList.length() - 1);
        deleteBuilder.where().in(SMSEntity.ID_COL, idList);
        deleteBuilder.delete();
    }
}
