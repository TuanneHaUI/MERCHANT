package com.teamphacode.MerchantManagement.domain.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MccUpdateRequest {
    String description;
    String descriptionEn;
    boolean isActive;
}
