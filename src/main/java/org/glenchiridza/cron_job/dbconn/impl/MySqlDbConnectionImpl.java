package org.glenchiridza.cron_job.dbconn.impl;

import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;

import java.sql.Connection;
import java.sql.ResultSet;

public class MySqlDbConnectionImpl implements MySqlDbConnection {

    public static Connection connection;

    @Override
    public ResultSet executeQuery(String sql) {
        return null;
    }

    @Override
    public Connection openConnection() {
        return null;
    }
}
