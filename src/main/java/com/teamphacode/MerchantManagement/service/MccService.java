package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;

import java.util.List;

public interface MccService {
    Mcc createMcc(Mcc request);
    Mcc updateMcc(String code, MccUpdateRequest request);
    List<Mcc> getActiveMccs();
}
