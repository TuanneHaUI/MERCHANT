package com.teamphacode.MerchantManagement.domain.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestResponse<T> {
    private int errorCode;
    private String errorDesc;
    private T data;
}