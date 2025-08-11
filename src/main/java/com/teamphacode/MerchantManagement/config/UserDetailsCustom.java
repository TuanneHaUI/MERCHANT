package com.teamphacode.MerchantManagement.config;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {

    private final UserServiceImpl userService;
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    public UserDetailsCustom(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = this.userService.handleGetUserByUsername(username);
        if (user == null) {
            logger.error("❌ Username/password không hợp lệ");
            throw new UsernameNotFoundException("Username/password không hợp lệ");
        }

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

}