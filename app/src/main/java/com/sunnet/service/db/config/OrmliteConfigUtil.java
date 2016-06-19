package com.sunnet.service.db.config;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 * <p/>
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class OrmliteConfigUtil extends OrmLiteConfigUtil {

    private static final String CONFIG_FILE_PATH_OSX = "/Users/nmtien92/Desktop/ormlite_config.txt";
    private static final String CONFIG_FILE_PATH_WINDOWS = "C:/Users/CR/Desktop/ormlite_config.txt";

    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile(new File(CONFIG_FILE_PATH_OSX));

    }

}
