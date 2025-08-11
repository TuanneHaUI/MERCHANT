package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;

public interface UserService {

     Users handleGetUserByUsername(String username);

     boolean isEmailExist(String email);

     void updateUserToken(String token, String email);

     Users getUserByRefreshTokenAndEmail(String refreshToken, String email);

     Users handleCreateUser(ReqRegister user) throws IdInvalidException;
}
