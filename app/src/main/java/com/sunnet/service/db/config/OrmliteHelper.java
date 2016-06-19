package com.sunnet.service.db.config;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.sunnet.service.application.MyApplication;
import com.sunnet.service.db.dao.ICallLogDao;
import com.sunnet.service.db.dao.ICallVoiceDao;
import com.sunnet.service.db.dao.ICaptureDao;
import com.sunnet.service.db.dao.IContactDao;
import com.sunnet.service.db.dao.ILocationDao;
import com.sunnet.service.db.dao.ISMSDao;
import com.sunnet.service.db.entity.CallLogEntity;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.db.entity.ContactEntity;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.util.SharedPreferencesUtility;
import com.sunnet.service.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.zip.ZipInputStream;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public class OrmliteHelper extends OrmLiteSqliteOpenHelper implements IOrmliteManager {

    /**
     * *********************************************
     * Suggested Copy/Paste dialCode. Everything from here to the done block.
     * **********************************************
     */

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "trackapp.sql";
    private static final String DATABASE_PATH = "/data/data/com.sunnet.service/databases/";
            /*Environment.getExternalStorageDirectory().getAbsolutePath() + "/databases/";*/

    private static final String ASSESS_PATH = "database/trackapp";

    static {
        Utils.newFolderIfNotExist(DATABASE_PATH);
        Log.d("hanh database path: [" + DATABASE_PATH + "]");
    }

    private Context mContext;

    public OrmliteHelper(Context context) {
        super(context, DATABASE_PATH + DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        initDatabase();
    }

    private void initDatabase() {

        String versionAppInstalled = SharedPreferencesUtility.getInstance()
                .getString(SharedPreferencesUtility.VERSION_APP_INSTALLED, "");
        try {
            if (!versionAppInstalled.equals(MyApplication.getContext().getPackageManager()
                    .getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionCode + ""))
                removeDatabaseIfExist();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!isDatabaseExist()) {
            copyDatabaseFromAssets();
        }
    }

    private void copyDatabaseFromAssets() {
        Log.w("database is copying from assets...");

        String path = ASSESS_PATH;
        String dest = DATABASE_PATH + DATABASE_NAME;
        InputStream is = null;
        boolean isZip = false;

        // try zip
        try {
            is = mContext.getAssets().open(path + ".zip");
            isZip = true;
        } catch (IOException e2) {
            try {
                // try uncompressed
                is = mContext.getAssets().open(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (is != null) {
            try {
                File f = new File(DATABASE_PATH);
                if (!f.exists()) {
                    f.mkdir();
                }
                if (isZip) {
                    ZipInputStream zis = Utils.getFileFromZip(is);
                    if (zis == null) {
                        Log.e("database copy error");
                        return;
                    }
                    Utils.writeExtractedFileToDisk(zis, new FileOutputStream(dest));
                } else {
                    Utils.writeExtractedFileToDisk(is, new FileOutputStream(dest));
                }

                Log.w("database copy complete");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.w("database copy error - file asset is not exist");
        }
    }

    /*
    * Check whether or not database exist
    */
    private void removeDatabaseIfExist() {
        String myPath = DATABASE_PATH + DATABASE_NAME;
        File dbFile = new File(myPath);
        boolean isDeleted = dbFile.delete();
        Log.i("hanh database old deleted [" + isDeleted + "]");
    }

    private boolean isDatabaseExist() {
        String myPath = DATABASE_PATH + DATABASE_NAME;
        File dbFile = new File(myPath);
        return dbFile.exists();
    }

    /**
     * Dao
     */
    private ICallVoiceDao iCallVoiceDao;
    private ICallLogDao iCallLogDao;
    private ICaptureDao iCaptureDao;
    private ILocationDao iLocationDao;
    private ISMSDao ismsDao;
    private IContactDao iContactDao;

    @Override
    public ICallVoiceDao getICallVoiceDao() throws SQLException {
        if (iCallVoiceDao == null)
            iCallVoiceDao = (ICallVoiceDao)
                    getDao(CallVoiceEntity.class);

        return iCallVoiceDao;
    }

    @Override
    public ICallLogDao getICallLogDao() throws SQLException {
        if (iCallLogDao == null)
            iCallLogDao = (ICallLogDao)
                    getDao(CallLogEntity.class);

        return iCallLogDao;
    }

    @Override
    public ICaptureDao getICaptureDao() throws SQLException {
        if (iCaptureDao == null)
            iCaptureDao = (ICaptureDao)
                    getDao(CaptureEntity.class);

        return iCaptureDao;
    }

    @Override
    public ILocationDao getILocationDao() throws SQLException {
        if (iLocationDao == null)
            iLocationDao = (ILocationDao)
                    getDao(LocationEntity.class);

        return iLocationDao;
    }

    @Override
    public ISMSDao getISMSDao() throws SQLException {
        if (ismsDao == null)
            ismsDao = (ISMSDao)
                    getDao(SMSEntity.class);

        return ismsDao;
    }

    @Override
    public IContactDao getIContactDao() throws SQLException {
        if (iContactDao == null)
            iContactDao = (IContactDao)
                    getDao(ContactEntity.class);

        return iContactDao;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
