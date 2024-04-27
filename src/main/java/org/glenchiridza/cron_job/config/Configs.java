package org.glenchiridza.cron_job.config;

import org.glenchiridza.cron_job.business.api.DeliveryService;
import org.glenchiridza.cron_job.business.impl.DeliveryServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configs {

    @Bean
    DeliveryService deliveryService(final ApplicationContext context){
        return new DeliveryServiceImpl(context);
    }

}
