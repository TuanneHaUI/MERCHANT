package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqCreatedUser;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.domain.dto.request.ResupdateUser;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.repository.UserRepository;
import com.teamphacode.MerchantManagement.service.UserService;
import com.teamphacode.MerchantManagement.util.LogUtil;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleServiceImpl roleService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private HttpServletRequest request;

    private LogUtil logUtil;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleServiceImpl roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
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
    @Transactional
    public Users handleCreateUser(ReqRegister user) throws IdInvalidException {
        if(user == null){
            throw new IdInvalidException("user null");
        }
        Users currentUser = new Users();
        currentUser.setEmail(user.getEmail());
        currentUser.setName(user.getFullName());
        currentUser.setPassword(this.passwordEncoder.encode(user.getPassword()));
        if (user.getRole() > 0 ) {
            Role r = this.roleService.fetchById(user.getRole());
            currentUser.setRole(r != null ? r : null);
        }
        return this.userRepository.save(currentUser);
    }

    @Override
    public ResultPaginationDTO fetchAllUser(Specification<Users> spec, Pageable pageable) {
        Page<Users> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);

//        List<ResUserDTO> listUser = pageUser.getContent()
//                .stream().map(item -> this.convertToResUserDTO(item))
//                .collect(Collectors.toList());

        rs.setResult(pageUser.getContent());

        return rs;
    }

    @Override
    @Transactional
    public Users handleUpdateUser(ResupdateUser reqUser) throws IdInvalidException {
        Users currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setAddress(reqUser.getAddress());
            currentUser.setGender(reqUser.getGender());
            currentUser.setAge(reqUser.getAge());
            currentUser.setName(reqUser.getName());



            // check role
            if (reqUser.getRole() != null ) {
                Role r = this.roleService.fetchById(reqUser.getRole().getId());
                currentUser.setRole(r != null ? r : null);
            }

            // update
            currentUser = this.userRepository.save(currentUser);
        }else{
            throw new IdInvalidException("User update in service null");
        }
        return currentUser;
    }

    @Override
    public Users fetchUserById(long id) {
        Optional<Users> userOptional = this.userRepository.findById(id);

        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    @Override
    public Users handleAdminCreateUser(ReqCreatedUser user) throws IdInvalidException {
        if(user == null){
            logger.warn("user create admin null{}{}", request.getMethod(), request.getRequestURI());
            throw new IdInvalidException("user create admin null");
        }else{
            Users currentUser = new Users();
            currentUser.setEmail(user.getEmail());
            currentUser.setPassword(this.passwordEncoder.encode(user.getPassword()));
            currentUser.setName(user.getName());
            currentUser.setGender(user.getGender());
            currentUser.setAddress(user.getAddress());
            currentUser.setAge(user.getAge());
            if(user.getRole() != null){
                Role currentRole = this.roleService.fetchById(user.getRole().getId());
                currentUser.setRole(currentRole != null ? currentRole : null);
            }
            logUtil.logJsonResponseService( logger,currentUser,"create user in  handleAdminCreateUser success ");
            return this.userRepository.save(currentUser);
        }


    }

}
