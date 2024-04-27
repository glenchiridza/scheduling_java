package org.glenchiridza.cron_job.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.glenchiridza.cron_job.business.api.DeliveryService;
import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;
import org.glenchiridza.cron_job.dbconn.api.PostgreDbConnection;
import org.glenchiridza.cron_job.model.MSDelivery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private static final String UPDATE_STATEMENT = "UPDATE";
    private final AtomicBoolean isBusy = new AtomicBoolean(false);

    @Value("${mysql-db.query.SQL_RETRIEVE_LAST_RUN}")
    private String lastDeliveryTime;

    private MySqlDbConnection mySqlDbConnection;

    private PostgreDbConnection postgreDbConnection;


    public DeliveryServiceImpl(final ApplicationContext context) {
        this.postgreDbConnection = context.getBean(PostgreDbConnection.class);
        this.mySqlDbConnection = context.getBean(MySqlDbConnection.class);
    }

    @Override
    public List<MSDelivery> getRecentDeliveries() {
        return null;
    }

    @Override
    public void getCurrentTime(String sql) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try{
            Connection connection = mySqlDbConnection.openConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            if(!sql.contains(UPDATE_STATEMENT)){
                preparedStatement.setString(1,timestamp.toString());
            }

            int affectedRows = preparedStatement.executeUpdate();
            if(affectedRows > 0){
                log.info("lastRecordDateTime: {}",affectedRows);
            }

        }catch (Exception ex){
            log.info("error : {}",ex.getMessage());
        }

    }

    private String getLastRunMysqlDB(){
        String lastTime = "";

        try{
            String query = lastDeliveryTime;
            ResultSet resultSet = mySqlDbConnection.executeQuery(query);

            if(resultSet == null){
                log.info("query return no lastRecordDateTime");
            }else{
                log.info("{}",resultSet);
                log.info("query found lastRecordDateTime");
                while(resultSet.next()){
                    lastTime = resultSet.getString("recordDateTime");
                    log.info("last recorded date and time :: {}",lastTime);
                }
            }

        }catch (SQLException ex){
            return null;
        }
        return lastTime;
    }
}
