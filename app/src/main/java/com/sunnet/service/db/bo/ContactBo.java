package com.sunnet.service.db.bo;

import com.sunnet.service.db.config.OrmliteManager;
import com.sunnet.service.db.dao.IContactDao;
import com.sunnet.service.db.entity.ContactEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class ContactBo implements IContactDao {
    public ContactBo() {
    }

    private IContactDao getDao() throws SQLException {
        return OrmliteManager.manager().getIContactDao();
    }

    @Override
    public void createAll(List<ContactEntity> list) throws SQLException {
        if (list.size() > 400) {
            int size = list.size();
            int countList = size / 400 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<ContactEntity> newList = new ArrayList<>();
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
    public void createOrUpdateEntity(ContactEntity entity) throws SQLException {
        getDao().createOrUpdateEntity(entity);
    }

    @Override
    public List<ContactEntity> getAll() throws SQLException {
        return getDao().getAll();
    }

    @Override
    public void updateAll(List<ContactEntity> list) throws SQLException {
        if (list.size() > 400) {
            int size = list.size();
            int countList = size / 400 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<ContactEntity> newList = new ArrayList<>();
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
    public void deleteEntity(ContactEntity entity) throws SQLException {
        getDao().deleteEntity(entity);
    }

    @Override
    public void deleteAll(List<ContactEntity> list) throws SQLException {
        getDao().deleteAll(list);
    }
}
