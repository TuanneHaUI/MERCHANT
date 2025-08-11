package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.LogStepByStep;
import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.repository.UserRepository;
import com.teamphacode.MerchantManagement.service.UserService;
import com.teamphacode.MerchantManagement.util.logging.LogUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final LogUtil logUtil;
    public UserServiceImpl(UserRepository userRepository,@Lazy LogUtil logUtil) {
        this.userRepository = userRepository;
        this.logUtil = logUtil;
    }
    public Users handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }



    @Override
    @LogStepByStep(tag = "Token Update")
    public void updateUserToken(String token, String email) {
        logUtil.step("Đang truy vấn DB để tìm người dùng với email: {}", email);
        Users currentUser = handleGetUserByUsername(email);

        if (currentUser != null) {
            logUtil.step("Tìm thấy user ID: {}. Đang gán token mới...", currentUser.getId());
            currentUser.setRefreshToken(token);
            logUtil.step("Đang thực hiện lệnh save vào DB...");
            this.userRepository.save(currentUser);
            logUtil.step("Lưu refresh token mới thành công.");
        } else {
            logUtil.step("Không tìm thấy người dùng. Bỏ qua.");
        }
    }

    public Users getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}
