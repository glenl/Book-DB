package com.vulturegraphics.glbd;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;

/**
 * Application database methods.
 */
class DBase {
    File dbPath = null;
    DateFormat isoDTFormat = null;

    private DBase() {
        Logger log = LoggerFactory.getLogger(DBase.class);
        String path = Config.getInstance().getProperty("glbd.dbpath");
        log.debug("db file is " + path);
        dbPath = new File(path);
    }

    public static DBase getInstance() {
        return DBConnectionHolder.INSTANCE;
    }

    public String getTimestamp() {
        return isoDTFormat.format(new Date());
    }

    public SQLiteQueue getQueue() throws SQLiteException {
        SQLiteQueue queue = new SQLiteQueue(dbPath);
        return queue.start();
    }

    private static class DBConnectionHolder {
        private static final DBase INSTANCE = new DBase();
    }
}
