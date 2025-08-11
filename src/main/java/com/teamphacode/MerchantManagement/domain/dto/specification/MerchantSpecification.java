package com.teamphacode.MerchantManagement.domain.dto.specification;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class MerchantSpecification {
    public static Specification<Merchant> filter(String merchantId, String accountNo, StatusEnum status){
        return  ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(merchantId != null && !merchantId.trim().isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), merchantId));
            }

            if(accountNo != null && !accountNo.trim().isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("accountNo"), accountNo));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
