package org.glenchiridza.cron_job.config;

import org.glenchiridza.cron_job.business.api.DeliveryService;
import org.glenchiridza.cron_job.business.impl.DeliveryServiceImpl;
import org.glenchiridza.cron_job.dbconn.api.MySqlDbConnection;
import org.glenchiridza.cron_job.dbconn.api.PostgreDbConnection;
import org.glenchiridza.cron_job.dbconn.impl.MySqlDbConnectionImpl;
import org.glenchiridza.cron_job.dbconn.impl.PostgreDbConnectionImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configs {

    @Bean
    PostgreDbConnection postgreDbConnection(){
        return new PostgreDbConnectionImpl();
    }

    @Bean
    MySqlDbConnection mySqlDbConnection(){
        return new MySqlDbConnectionImpl();
    }

    @Bean
    DeliveryService deliveryService(final ApplicationContext context){
        return new DeliveryServiceImpl(context);
    }

}
