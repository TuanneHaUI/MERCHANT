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
public class BranchCode {
    @Id
    @Column(length = 4, nullable = false, unique = true)
    String code;
    @Column(nullable = false)
    String name;

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
