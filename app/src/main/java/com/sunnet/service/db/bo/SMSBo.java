package com.sunnet.service.db.bo;

import com.sunnet.service.db.config.OrmliteManager;
import com.sunnet.service.db.dao.ISMSDao;
import com.sunnet.service.db.entity.SMSEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class SMSBo implements ISMSDao {
    public SMSBo() {
    }

    private ISMSDao getDao() throws SQLException {
        return OrmliteManager.manager().getISMSDao();
    }

    @Override
    public void createAll(List<SMSEntity> list) throws SQLException {
        if (list.size() > 400) {
            int size = list.size();
            int countList = size / 400 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<SMSEntity> newList = new ArrayList<>();
                for (int j = next; j < size; j++) {
                    newList.add(list.get(j));
                    if (j >= (400 * (i + 1))) {
                        next = j;
                        break;
                    }
                }
                if (newList.size() > 0) {
                    getDao().createAll(newList);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else
            getDao().createAll(list);
    }

    @Override
    public void createOrUpdateEntity(SMSEntity entity) throws SQLException {
        getDao().createOrUpdateEntity(entity);
    }

    @Override
    public List<SMSEntity> getAll() throws SQLException {
        return getDao().getAll();
    }

    @Override
    public List<SMSEntity> getAllByStatus(int status) throws SQLException {
        return getDao().getAllByStatus(status);
    }

    @Override
    public void updateAll(List<SMSEntity> list) throws SQLException {
        if (list.size() > 400) {
            int size = list.size();
            int countList = size / 400 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<SMSEntity> newList = new ArrayList<>();
                for (int j = next; j < size; j++) {
                    newList.add(list.get(j));
                    if (j >= (400 * (i + 1))) {
                        next = j;
                        break;
                    }
                }
                if (newList.size() > 0) {
                    getDao().updateAll(newList);
                }
            }
        } else
            getDao().updateAll(list);
    }

    @Override
    public void deleteEntity(SMSEntity entity) throws SQLException {
        getDao().deleteEntity(entity);
    }

    @Override
    public void deleteAll(List<SMSEntity> list) throws SQLException {
        getDao().deleteAll(list);
    }
}
