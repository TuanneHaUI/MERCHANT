package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Permission;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;



public interface PermissionService {

    boolean isPermissionExist(Permission p);

    Permission fetchById(long id);

    Permission create(Permission p);

    Permission update(Permission p);

    void delete(long id);

    ResultPaginationDTO getPermissions(Specification<Permission> spec, Pageable pageable);

    boolean isSameName(Permission p);

}
