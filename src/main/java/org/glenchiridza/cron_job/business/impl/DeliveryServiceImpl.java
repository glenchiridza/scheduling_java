package org.glenchiridza.cron_job.business.impl;

import lombok.extern.slf4j.Slf4j;
import org.glenchiridza.cron_job.business.api.DeliveryService;
import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;
import org.glenchiridza.cron_job.dbconn.api.PostgreDbConnection;
import org.glenchiridza.cron_job.model.MSDelivery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Value("${postgre-db.query.delivery}")
    private String delivery;

    @Value("${mysql-db.query.dataInsertion}")
    private String deliveryTransfer;

    @Value("${mysql-db.query.SQL_RETRIEVE_LAST_RUN}")
    private String lastDeliveryTime;

    @Value("${mysql-db.query.SQL_INSERT_LAST_RUN}")
    private String sql_insert;
    @Value("${mysql-db.query.SQL_UPDATE_LAST_RUN}")
    private String sql_update;


    private final MySqlDbConnection mySqlDbConnection;

    private final PostgreDbConnection postgreDbConnection;


    public DeliveryServiceImpl(final ApplicationContext context) {
        this.postgreDbConnection = context.getBean(PostgreDbConnection.class);
        this.mySqlDbConnection = context.getBean(MySqlDbConnection.class);
    }


    @Scheduled(cron ="*/20 * * * * *")
    public void deliverySchedule(){
        log.info("starting scheduler.");
        {
            try{

                List<MSDelivery> deliveries = getRecentDeliveries();
                log.info("deliveries exported from postgres db into mysql db : \n {}",deliveries);
                updateDeliverMysqlTable(deliveries);
                String lastMSDBRunDateTime = getLastRunMysqlDB();
                if(lastMSDBRunDateTime == null || lastMSDBRunDateTime.isEmpty()){
                    getCurrentTime(sql_insert);
                }else{
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    getCurrentTime(sql_update.replace("{lastRunDateTime}",timestamp.toString()));
                }
            }catch (Exception ex){
                log.info("Program on thread: {} ... failed with error ... {}",Thread.currentThread().getId(),ex.toString());
            }finally {

                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public List<MSDelivery> getRecentDeliveries() {
        List<MSDelivery> deliveries = new ArrayList<>();
        String lastRunDateTime = getLastRunMysqlDB();
        String query;
        String currentTime = LocalDateTime.now().toString();

        log.info("last run on mysqldb: {}",lastRunDateTime);

        if(!lastRunDateTime.isEmpty()){
            query = delivery.replace("{lastRunDateTime}",lastRunDateTime)
                    .replace("{current_date}",currentTime);
        }else{
            //on start run, in case the last run date-time table has nothing on first run, start from the time just when the db was first created
            query = delivery.replace("{lastRunDateTime}",LocalDateTime.now().minusYears(1).toString())
                    .replace("{current_date}",currentTime);
        }

        try{

            System.out.println(query);
            ResultSet resultSet = postgreDbConnection.executeQuery(query);
            if(resultSet == null){
                log.info("query returned 0 deliveries");
            }else{
                log.info("found new deliveries");
                while (resultSet.next()){
                    MSDelivery msDelivery = new MSDelivery();
                    msDelivery.setId(resultSet.getInt("id"));
                    msDelivery.setClientId(resultSet.getInt("client_id"));
                    msDelivery.setCreatedAt(resultSet.getDate("created_at"));
                    msDelivery.setDeliveryCompany(resultSet.getString("delivery_company"));
                    msDelivery.setReceivedDelivery(resultSet.getBoolean("received_delivery"));

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
                preparedStatement.setString(1,LocalDateTime.now().toString());
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
                    lastTime = resultSet.getString("record_date_time");
                    log.info("last recorded date and time :: {}",lastTime);
                }
            }

        }catch (SQLException ex){
            return null;
        }
        return lastTime;
    }
}
