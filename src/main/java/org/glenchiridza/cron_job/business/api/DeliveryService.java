package org.glenchiridza.cron_job.business.api;

public interface DeliveryService {

    List<MSDelivery> getRecentDeliveries();

    void getCurrentTime(String sql);
}
