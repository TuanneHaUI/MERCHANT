package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqLoginDTO;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqRegister;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqSendOtp;
import com.teamphacode.MerchantManagement.domain.dto.response.ResCreateUserDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.ResLoginDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.RestloginOtp;
import com.teamphacode.MerchantManagement.service.impl.EmailServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.OtpServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.UserServiceImpl;
import com.teamphacode.MerchantManagement.util.SecurityUtil;
import com.teamphacode.MerchantManagement.util.annotation.ApiMessage;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailServiceImpl;
    private final OtpServiceImpl otpService;
    @Value("${tuanne.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                          UserServiceImpl userService, PasswordEncoder passwordEncoder, EmailServiceImpl emailServiceImpl, OtpServiceImpl otpService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailServiceImpl = emailServiceImpl;
        this.otpService = otpService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        // create a token
        // set thông tin người dùng đăng nhập vào context( có thể dùng sao này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        Users currentUserDB = userService.handleGetUserByUsername(loginDto.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(), currentUserDB.getRole());
            res.setUser(userLogin);
        }
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);

        // update user
        this.userService.updateUserToken(refresh_token, loginDto.getUsername());

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        Users currentUserDB = userService.handleGetUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok().body(userGetAccount);
    }
//    @PostMapping("/auth/login")
//    @ApiMessage("Login susscess")
//    public ResponseEntity<?> login(@RequestBody ReqLoginDTO loginDto) throws IdInvalidException {
//            if (loginDto.getUsername() == null || loginDto.getUsername().isEmpty() ||
//                    loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
//                return ResponseEntity.badRequest().body("Username và password không được để trống");
//            }
//
//            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                    loginDto.getUsername(), loginDto.getPassword());
//
//            Authentication authentication = authenticationManagerBuilder.getObject()
//                    .authenticate(authenticationToken);
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            String otp = String.format("%06d", new Random().nextInt(1_000_000));
//            otpService.saveOtp(loginDto.getUsername(), otp);
//
//            this.emailServiceImpl.sendOtpMessage(loginDto.getUsername(), otp);
//
//            RestloginOtp restloginOtp = new RestloginOtp();
//            restloginOtp.setEmail(loginDto.getUsername());
//            restloginOtp.setDescription("OTP đã được gửi đến email của bạn");
//            return ResponseEntity.ok(restloginOtp);
//
//    }
//
//    @PostMapping("/auth/verify-otp")
//    @ApiMessage("otp susscess")
//    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) throws Exception {
//        try {
//            String email = request.get("email");
//            String otp = request.get("otp");
//
//            if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
//                return ResponseEntity.badRequest().body("Email và OTP không được để trống");
//            }
//
//            if (!otpService.verifyOtp(email, otp)) {
//                throw new IdInvalidException("otp đã hết hạn hoặc không đúng");
//            }
//
//            Users currentUser = userService.handleGetUserByUsername(email);
//            if (currentUser == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản không tồn tại");
//            }
//
//            ResLoginDTO res = new ResLoginDTO();
//            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
//                    currentUser.getId(), currentUser.getEmail(), currentUser.getName());
//            res.setUser(userLogin);
//
//            String access_token = securityUtil.createAccessToken(currentUser.getEmail(), res);
//            String refresh_token = securityUtil.createRefreshToken(currentUser.getEmail(), res);
//
//            res.setAccessToken(access_token);
//            userService.updateUserToken(refresh_token, currentUser.getEmail());
//
//            ResponseCookie resCookies = ResponseCookie
//                    .from("refresh_token", refresh_token)
//                    .httpOnly(true)
//                    .secure(true)
//                    .path("/")
//                    .maxAge(refreshTokenExpiration)
//                    .build();
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.SET_COOKIE, resCookies.toString())
//                    .body(res);
//
//        } catch (Exception e) {
//            logger.error("Lỗi verify OTP", e);
//            throw new Exception("Lỗi verify OTP");
//        }
//    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token)
            throws IdInvalidException {
        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở Cookies");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();
        // check user by token + email
        Users currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        System.out.println("User "+currentUser);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ");
        }

        // issue new token/set refresh token as cookies
        ResLoginDTO res = new ResLoginDTO();
        Users currentUserDB = userService.handleGetUserByUsername(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(), currentUserDB.getRole());
            res.setUser(userLogin);
        }
        String access_token = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);

        // update user
        this.userService.updateUserToken(new_refresh_token, email);

        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("logout susscess")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access token không hợp lệ");
        }

        this.userService.updateUserToken(null, email);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

    @PostMapping("/auth/register")
    @ApiMessage("Register otp")
    public ResponseEntity<?> register(@Valid @RequestBody ReqSendOtp postManUser) throws IdInvalidException {
        if(postManUser == null){
            throw new IdInvalidException("User null");
        }
        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if(isEmailExist){
            throw new IdInvalidException("Email đã tồn tại vui lòng dùng tài khoản khác");
        }
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpService.saveOtp(postManUser.getEmail(), otp);

        this.emailServiceImpl.sendOtpMessage(postManUser.getEmail(), otp);

        RestloginOtp restloginOtp = new RestloginOtp();
        restloginOtp.setEmail(postManUser.getEmail());
        restloginOtp.setDescription("OTP đã được gửi đến email của bạn");
        return ResponseEntity.ok(restloginOtp);
    }
    @PostMapping("/auth/otp/register")
    @ApiMessage("Register success")
    public ResponseEntity<?> registerOtp(@Valid @RequestBody ReqRegister postManUser) throws IdInvalidException {

        if (postManUser.getEmail() == null || postManUser.getOtp() == null) {
            return ResponseEntity.badRequest().body("Email và OTP không được để trống");
        }

        if (!otpService.verifyOtp(postManUser.getEmail(), postManUser.getOtp())) {
            throw new IdInvalidException("otp đã hết hạn hoặc không đúng");
        }
        if(!postManUser.getPassword().equals(postManUser.getConfirmPassword())){
            throw new IdInvalidException("Mật khẩu không hợp lệ");
        }
        Users currentUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.ok(currentUser);
    }

}