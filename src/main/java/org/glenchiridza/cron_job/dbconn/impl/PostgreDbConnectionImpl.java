package org.glenchiridza.cron_job.dbconn.impl;

import lombok.extern.slf4j.Slf4j;
import org.glenchiridza.cron_job.dbconn.api.PostgreDbConnection;
import org.springframework.beans.factory.annotation.Value;

import java.sql.*;

@Slf4j
public class PostgreDbConnectionImpl implements PostgreDbConnection {

    private Connection connection;

    @Value("${spring.datasource-ps.driver-class-name}")
    private String jdbcClassName;

    @Value("${spring.datasource-ps.url}")
    private String url;

    @Value("${spring.datasource-ps.username}")
    private String username;

    @Value("${spring.datasource-ps.password}")
    private String password;

    @Override
    public Connection openConnection() throws Exception {
        if(connection == null || connection.isClosed()){
            try {
                log.debug("connecting to : {}",url);
                Class.forName(jdbcClassName);
                connection = DriverManager.getConnection(url,username,password);
                return connection;
            }catch (SQLException | ClassNotFoundException e){
                log.error("Error opening connection");
                throw new SQLException("Failed to connect to PostgreSQL Database: {}",e.getMessage());
            }
        }
        return connection;
    }

    @Override
    public ResultSet executeQuery(String sql) {
        ResultSet resultSet = null;
        try{
            log.debug(sql);
            openConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sql);
        } catch (Exception ex) {
            log.error("SQL query error: ",ex);
        }
        return resultSet;
    }


}
