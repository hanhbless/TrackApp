package com.sunnet.service.db.dao;

import android.os.Build;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.util.Utils;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class CallVoiceDao extends BaseDaoImpl<CallVoiceEntity, String> implements ICallVoiceDao {
    public CallVoiceDao(Class<CallVoiceEntity> dataClass) throws SQLException {
        super(dataClass);
    }

    public CallVoiceDao(ConnectionSource connectionSource, Class<CallVoiceEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public CallVoiceDao(ConnectionSource connectionSource, DatabaseTableConfig<CallVoiceEntity> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public void createAll(List<CallVoiceEntity> list) throws SQLException {
        if (list == null || list.isEmpty())
            return;

        //-- Check system version because Ormlite 4.48 only support android version >= JELLY_BEAN
        //-- multiple: insert, update, delete
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (CallVoiceEntity item : list) {
                createOrUpdate(item);
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO '" + CallVoiceEntity.TABLE_NAME + "'");
        builder.append("(");
        builder.append("'" + CallVoiceEntity.ID_COL + "', ");
        builder.append("'" + CallVoiceEntity.DATE_COL + "', ");
        builder.append("'" + CallVoiceEntity.PHONE_NUMBER_COL + "', ");
        builder.append("'" + CallVoiceEntity.PHONE_NAME_COL + "', ");
        builder.append("'" + CallVoiceEntity.AUDIO_COL + "'");
        builder.append(")");
        builder.append(" VALUES ");

        int count = list.size();
        for (int i = 0; i < count; i++) {
            CallVoiceEntity item = list.get(i);
            builder.append("(");
            builder.append("'" + item.getId() + "', ");
            builder.append("'" + item.getDate() + "', ");
            builder.append("'" + item.getPhoneNumber() + "', ");
            builder.append("'" + item.getPhoneName() + "', ");
            builder.append("'" + item.getAudio() + "'");
            builder.append(")");

            if (i < count - 1)
                builder.append(",");
        }
        builder.append(";");

        //-- Execute sql
        executeRawNoArgs(builder.toString());
    }

    @Override
    public void createOrUpdateEntity(CallVoiceEntity entity) throws SQLException {
        createOrUpdate(entity);
    }

    @Override
    public List<CallVoiceEntity> getAll() throws SQLException {
        return queryForAll();
    }

    @Override
    public void deleteEntity(CallVoiceEntity entity) throws SQLException {
        delete(entity);
    }

    @Override
    public void deleteAll(List<CallVoiceEntity> list) throws SQLException {
        String idList = Utils.getStringCallVoiceId(list);

        if (Utils.isEmptyString(idList))
            return;

        DeleteBuilder<CallVoiceEntity, String> deleteBuilder = deleteBuilder();
        // Remove 'quote' in the first and bottom of String Because of [Where In Clause in Ormlite format: "('" + value_1' + 'value_2' + ... 'value_n + "')" ]
        idList = idList.substring(1, idList.length() - 1);
        deleteBuilder.where().in(CallVoiceEntity.ID_COL, idList);
        deleteBuilder.delete();
    }
}
