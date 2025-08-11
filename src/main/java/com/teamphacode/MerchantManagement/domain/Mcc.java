package com.teamphacode.MerchantManagement.domain;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Mcc {

    @Id
    @Column(name = "mcc", length = 4)
    private String code;

    @Column(name = "description", length = 255, nullable = false)
    String description;

    @Column(name = "description_en", length = 255)
    String descriptionEn;

    @Column(name = "is_active", nullable = false)
    boolean isActive = true;
}
