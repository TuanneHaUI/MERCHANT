package com.teamphacode.MerchantManagement.domain.dto.response;

import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResMerchantYearStatusDTO {
    private int year;
    private String status;
    private long total;
}
