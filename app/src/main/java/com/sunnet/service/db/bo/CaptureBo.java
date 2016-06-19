package com.sunnet.service.db.bo;

import com.sunnet.service.db.config.OrmliteManager;
import com.sunnet.service.db.dao.ICaptureDao;
import com.sunnet.service.db.entity.CaptureEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class CaptureBo implements ICaptureDao {
    public CaptureBo() {
    }

    private ICaptureDao getDao() throws SQLException {
        return OrmliteManager.manager().getICaptureDao();
    }

    @Override
    public void createAll(List<CaptureEntity> list) throws SQLException {
        getDao().createAll(list);
    }

    @Override
    public void createOrUpdateEntity(CaptureEntity entity) throws SQLException {
        getDao().createOrUpdateEntity(entity);
    }

    @Override
    public List<CaptureEntity> getAll() throws SQLException {
        return getDao().getAll();
    }

    @Override
    public void deleteEntity(CaptureEntity entity) throws SQLException {
        getDao().deleteEntity(entity);
    }
}
