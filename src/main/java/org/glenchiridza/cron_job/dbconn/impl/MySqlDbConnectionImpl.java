package org.glenchiridza.cron_job.dbconn.impl;

import lombok.extern.slf4j.Slf4j;
import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;
import org.springframework.beans.factory.annotation.Value;

import java.sql.*;

@Slf4j
public class MySqlDbConnectionImpl implements MySqlDbConnection {

    public static Connection connection;

    @Value("${spring.datasource.driver-class-name}")
    private String jdbcClassName;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    @Override
    public Connection openConnection() throws Exception{
        if(connection == null || connection.isClosed()){
            try{
                log.debug("Connecting to :{}",url);
                Class.forName(jdbcClassName);
                connection = DriverManager.getConnection(url,username,password);
                return connection;

            } catch (SQLException | ClassNotFoundException e) {
                log.error("Failed to establish connection to DB");
                throw new SQLException("Failed to connect to database");
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
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.executeQuery(sql);
            resultSet = statement.getResultSet();
        }catch (Exception ex){
            log.error("Error in SQL: {}",ex.getMessage());
        }
        return resultSet;
    }

}
