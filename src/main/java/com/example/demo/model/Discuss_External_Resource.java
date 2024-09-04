package com.ispan.eeit188_final.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "Discuss_External_Resource") // 討論區外部資源
public class Discuss_External_Resource {

    @Column(name = "ur", columnDefinition = "VARCHAR(MAX)")
    private String ur;

    @Column(name = "discuss_id", columnDefinition = "UUID")
    private UUID DiscussId;

    @Column(name = "type", columnDefinition = "VARCHAR(10)")
    private String type;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

}
