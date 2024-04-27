package org.glenchiridza.cron_job.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.glenchiridza.cron_job.business.api.DeliveryService;
import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;
import org.glenchiridza.cron_job.dbconn.api.PostgreDbConnection;
import org.glenchiridza.cron_job.model.MSDelivery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private static final String UPDATE_STATEMENT = "UPDATE";
    private final AtomicBoolean isBusy = new AtomicBoolean(false);

    @Value("${mysql-db.query.delivery}")
    private String delivery;

    @Value("${mysql-db.query.dataInsertion}")
    private String deliveryTransfer;

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
        List<MSDelivery> deliveries = new ArrayList<>();
        String lastRunDateTime = getLastRunMysqlDB();
        String query;
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("last run on mysqldb: {}",lastRunDateTime);
        if(!lastRunDateTime.isEmpty()){
            query = delivery.replace("lastRunDateTime",lastDeliveryTime)
                    .replace("current_date",currentTime);
        }else{
            //on start run, in case the last run date-time table has nothing on first run, start from the time just when the db was first created
            String oldTime = LocalDateTime.of(2023,1,1,0,2).toString();
            query = delivery.replace("lastRunDateTime",oldTime)
                    .replace("current_date",currentTime);
        }

        try{
            ResultSet resultSet = postgreDbConnection.executeQuery(query);
            if(resultSet == null){
                log.info("query return 0 deliveries");
            }else{
                log.info("found new deliveries");
                while (resultSet.next()){
                    MSDelivery msDelivery = new MSDelivery();
                    msDelivery.setId(resultSet.getInt(""));
                    msDelivery.setClientId(resultSet.getInt(""));
                    msDelivery.setDeliveryCompany(resultSet.getString(""));
                    msDelivery.setReceivedDelivery(resultSet.getBoolean(""));
                    msDelivery.setCreatedAt(resultSet.getTimestamp(2).toLocalDateTime());

                    deliveries.add(msDelivery);
                    log.info("deliveries :: {}",deliveries);
                }

            }
        }catch (SQLException ex){
            return null;
        }

        return deliveries;
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

    private void updateDeliverMysqlTable(List<MSDelivery> deliveries){

        for(MSDelivery delivery : deliveries){
            try{
                String sql = deliveryTransfer;
                Connection connection = mySqlDbConnection.openConnection();

                PreparedStatement preparedStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);

                preparedStatement.setInt(1,delivery.getId());
                preparedStatement.setInt(2,delivery.getClientId());
                preparedStatement.setString(3,delivery.getDeliveryCompany());
                preparedStatement.setBoolean(4,delivery.getReceivedDelivery());
                preparedStatement.setObject(5,delivery.getCreatedAt());

                int rows = preparedStatement.executeUpdate();
                if(rows > 0){
                    log.info("new deliveries: {}",rows);
                }
            }
            catch (Exception ex){
                log.error("error :: {}",ex.getMessage());
            }
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
