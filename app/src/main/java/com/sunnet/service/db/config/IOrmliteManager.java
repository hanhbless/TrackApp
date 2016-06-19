package com.sunnet.service.db.config;

import com.sunnet.service.db.dao.ICallLogDao;
import com.sunnet.service.db.dao.ICallVoiceDao;
import com.sunnet.service.db.dao.ICaptureDao;
import com.sunnet.service.db.dao.IContactDao;
import com.sunnet.service.db.dao.ILocationDao;
import com.sunnet.service.db.dao.ISMSDao;

import java.sql.SQLException;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public interface IOrmliteManager {
    ICallVoiceDao getICallVoiceDao() throws SQLException;

    ICallLogDao getICallLogDao() throws SQLException;

    ICaptureDao getICaptureDao() throws SQLException;

    ILocationDao getILocationDao() throws SQLException;

    ISMSDao getISMSDao() throws SQLException;

    IContactDao getIContactDao() throws SQLException;

}
