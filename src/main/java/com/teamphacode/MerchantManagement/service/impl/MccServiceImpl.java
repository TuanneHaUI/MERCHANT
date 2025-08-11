package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.repository.MccRepository;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MccService;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MccServiceImpl implements MccService {
    @Autowired
    private MccRepository mccRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Override
    public Mcc createMcc(Mcc request) {
        if(mccRepository.existsById(request.getCode())) throw new AppException(request.getCode() + "existed");
        return mccRepository.save(request);
    }

    @Override
    public Mcc updateMcc(String code, MccUpdateRequest request) {
        Mcc mcc = mccRepository.findById(code)
                .orElseThrow(() -> new AppException("MCC not found with code: " + code));

        mcc.setDescription(request.getDescription() != null ? request.getDescription() : mcc.getDescription());
        mcc.setDescriptionEn(request.getDescriptionEn() != null ? request.getDescriptionEn() : mcc.getDescriptionEn());
        mcc.setActive(request.isActive() != mcc.isActive() ? request.isActive() : mcc.isActive());
        return mccRepository.save(mcc);
    }

    @Override
    public List<Mcc> getActiveMccs() {
        return mccRepository.findByIsActiveTrueOrderByCode();
    }
}
