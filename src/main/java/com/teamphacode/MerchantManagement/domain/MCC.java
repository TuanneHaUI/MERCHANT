package com.teamphacode.MerchantManagement.domain;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class MCC {
    @Id
    @Column(length = 4, nullable = false, unique = true)
    String code;
    @Column(nullable = false)
    String description;

    Instant createdAt;
    Instant updatedAt;
    String contentUpdate;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }

}
