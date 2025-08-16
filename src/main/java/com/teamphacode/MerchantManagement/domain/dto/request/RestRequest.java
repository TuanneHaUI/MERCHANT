package com.teamphacode.MerchantManagement.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestRequest<T> {
    @NotBlank
    @Size(max = 36)
    private String requestId;

    @NotBlank
    @Size(max = 20)
    private String requestTime; // Format GMT+7

    private T data;
}
