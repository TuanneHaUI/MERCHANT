package com.teamphacode.MerchantManagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqCreatedUser;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.domain.dto.request.ResupdateUser;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.repository.RoleRepository;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleServiceImpl roleService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HASH_KEY = "UserCache";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
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
        Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
        if(adminRole != null){
            currentUser.setRole(adminRole);
        }else{
            logger.error("Lỗi trong việc tạo role cho user");
            throw new IdInvalidException("Lỗi trong việc tạo role cho user");
        }

        return this.userRepository.save(currentUser);
    }

    @Override
    public ResultPaginationDTO fetchAllUser(Specification<Users> spec, Pageable pageable) {
        //logger.info("Start find user {} {}",request.getMethod(), request.getRequestURI());
        //String cacheKey = generateCacheKey(pageable, spec);

//        Object cachedObj = redisTemplate.opsForHash().get(HASH_KEY, cacheKey);
//        if (cachedObj != null) {
//            try {
//                ResultPaginationDTO cachedData = new ObjectMapper()
//                        .convertValue(cachedObj, ResultPaginationDTO.class);
//               //logUtil.logJsonResponseService(logger,cachedData,"findAll user in redis success");
//                return cachedData;
//            } catch (IllegalArgumentException e) {
//                logger.warn("⚠️ Lỗi chuyển đổi cachedData sang ResultPaginationDTO, bỏ qua cache", e);
//            }
//        }
        Page<Users> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageUser.getContent());

//        redisTemplate.opsForHash().put(HASH_KEY, cacheKey, rs);
//        redisTemplate.expire(HASH_KEY, Duration.ofHours(1));
        //logUtil.logJsonResponseService(logger,rs,"findAll user in service success");
        return rs;
    }

//    private String generateCacheKey(Pageable pageable, Specification<Users> spec) {
//        StringBuilder keyBuilder = new StringBuilder();
//        keyBuilder.append("findAllUser").append("page:").append(pageable.getPageNumber())
//                .append(":size:").append(pageable.getPageSize());
//
//        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
//            keyBuilder.append(":sort:");
//            pageable.getSort().forEach(order -> {
//                keyBuilder.append(order.getProperty())
//                        .append(",")
//                        .append(order.getDirection())
//                        .append(";");
//            });
//            keyBuilder.append(":spec:");
//            if(spec != null){
//                keyBuilder.append(Objects.hashCode(spec.toString()));
//            }else {
//                keyBuilder.append("no_spec");
//            }
//        }
//        System.out.println("Key tổng"+keyBuilder);
//        return keyBuilder.toString();
//    }

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

    @Override
    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

}
