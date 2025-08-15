package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MccService {
    Mcc createMcc(Mcc request);
    Mcc updateMcc(String code, MccUpdateRequest request);
    ResultPaginationDTO getAllMccs(Pageable pageable);
    void deleteMcc(String code);
}
