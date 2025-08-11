package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.repository.UserRepository;
import com.teamphacode.MerchantManagement.service.UserService;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Users handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }



    @Override
    public void updateUserToken(String token, String email) {
        Users currentUser = handleGetUserByUsername(email);

        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        } else {

        }
    }

    public Users getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    @Override
    public Users handleCreateUser(ReqRegister user) throws IdInvalidException {
        if(user == null){
            throw new IdInvalidException("user null");
        }
        Users currentUser = new Users();
        currentUser.setEmail(user.getEmail());
        currentUser.setName(user.getFullName());
        currentUser.setPassword(this.passwordEncoder.encode(user.getPassWord()));
        return this.userRepository.save(currentUser);
    }

}
