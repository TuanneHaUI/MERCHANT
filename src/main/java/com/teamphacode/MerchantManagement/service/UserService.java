package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Users;

public interface UserService {

     Users handleGetUserByUsername(String username);

     boolean isEmailExist(String email);

     void updateUserToken(String token, String email);

     Users getUserByRefreshTokenAndEmail(String refreshToken, String email);
}
