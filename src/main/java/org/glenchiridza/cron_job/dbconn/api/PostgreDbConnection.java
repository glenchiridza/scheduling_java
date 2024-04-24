package org.glenchiridza.cron_job.dbconn.api;

import java.sql.Connection;
import java.sql.ResultSet;

public interface PostgreDbConnection {
    ResultSet executeQuery(String sql);
    Connection openConnection();
}
