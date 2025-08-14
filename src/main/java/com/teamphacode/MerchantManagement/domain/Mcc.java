package com.teamphacode.MerchantManagement.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "code: không được để trống")
    @Size(max = 4, message = "code: không vượt quá 4 ký tự")
    private String code;


    @Column(name = "description", length = 255, nullable = false)
    String description;

    @Column(name = "description_en", length = 255)
    String descriptionEn;

    @Column(name = "is_active", nullable = false)
    boolean isActive = true;
}
