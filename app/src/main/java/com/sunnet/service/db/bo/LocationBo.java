package com.sunnet.service.db.bo;

import com.sunnet.service.db.config.OrmliteManager;
import com.sunnet.service.db.dao.ILocationDao;
import com.sunnet.service.db.entity.LocationEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class LocationBo implements ILocationDao {
    public LocationBo() {
    }

    private ILocationDao getDao() throws SQLException {
        return OrmliteManager.manager().getILocationDao();
    }

    @Override
    public void createAll(List<LocationEntity> list) throws SQLException {
        if (list.size() > 400) {
            int size = list.size();
            int countList = size / 400 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<LocationEntity> newList = new ArrayList<>();
                for (int j = next; j < size; j++) {
                    newList.add(list.get(j));
                    if (j >= (400 * (i + 1))) {
                        next = j;
                        break;
                    }
                }
                if (newList.size() > 0) {
                    getDao().createAll(newList);
                }
            }
        } else
            getDao().createAll(list);
    }

    @Override
    public void createOrUpdateEntity(LocationEntity entity) throws SQLException {
        getDao().createOrUpdateEntity(entity);
    }

    @Override
    public List<LocationEntity> getAll() throws SQLException {
        return getDao().getAll();
    }

    @Override
    public LocationEntity getLastEntity() throws SQLException {
        return getDao().getLastEntity();
    }

    @Override
    public void deleteEntity(LocationEntity entity) throws SQLException {
        getDao().deleteEntity(entity);
    }

    @Override
    public void deleteAll(List<LocationEntity> list) throws SQLException {
        getDao().deleteAll(list);
    }
}
