package org.glenchiridza.cron_job.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MSDelivery {

    @Id
    @GeneratedValue(generator = "sequence_delivery",strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "sequence_delivery",sequenceName = "sequence_delivery")
    private Integer id;
    private Integer clientId;
    private String deliveryCompany;
    private Boolean receivedDelivery;
    private LocalDateTime createdAt;
}