package org.glenchiridza.cron_job;

import org.glenchiridza.cron_job.config.Configs;
import org.glenchiridza.cron_job.config.ScheduleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({Configs.class, ScheduleConfig.class})
public class CronJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(CronJobApplication.class, args);
    }

}
