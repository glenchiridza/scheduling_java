package org.glenchiridza.cron_job.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MSDelivery {

    @Id
    private Integer id;
    private Integer clientId;
    private String deliveryCompany;
    private Boolean receivedDelivery;
    private Date createdAt;
}