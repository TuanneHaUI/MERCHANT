package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqCreatedUser;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.domain.dto.request.ResupdateUser;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {

     Users handleGetUserByUsername(String username);

     boolean isEmailExist(String email);

     void updateUserToken(String token, String email);

     Users getUserByRefreshTokenAndEmail(String refreshToken, String email);

     Users handleCreateUser(ReqRegister user) throws IdInvalidException;

     ResultPaginationDTO fetchAllUser(Specification<Users> spec, Pageable pageable);

     Users handleUpdateUser(ResupdateUser reqUser) throws IdInvalidException;

     Users fetchUserById(long id);

     Users handleAdminCreateUser(ReqCreatedUser user) throws IdInvalidException;
}
