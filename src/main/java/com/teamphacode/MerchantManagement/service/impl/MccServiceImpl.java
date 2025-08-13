package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.repository.MccRepository;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MccService;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
        if(mccRepository.existsById(request.getCode())) throw new AppException("Mã MCC '" + request.getCode() + "' đã tồn tại.", HttpStatus.BAD_REQUEST.value());
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
    public ResultPaginationDTO getAllMccs(Pageable pageable) {
        Page<Mcc> mccPage = this.mccRepository.findAll(pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber()+ 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(mccPage.getTotalPages());
        mt.setTotal(mccPage.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(mccPage.getContent());
        return rs;
    }

    @Override
    public void deleteMcc(String code) {
        if(!mccRepository.existsById(code)) throw new AppException("not found code");
        mccRepository.deleteById(code);
    }
}
