package org.glenchiridza.cron_job.business.api;

import org.glenchiridza.cron_job.model.MSDelivery;

import java.util.List;

public interface DeliveryService {

    List<MSDelivery> getRecentDeliveries();

    void getCurrentTime(String sql);
}
