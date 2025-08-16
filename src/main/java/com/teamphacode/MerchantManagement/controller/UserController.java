package com.teamphacode.MerchantManagement.controller;


import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqCreatedUser;
import com.teamphacode.MerchantManagement.domain.dto.request.ResupdateUser;
import com.teamphacode.MerchantManagement.domain.dto.response.ResCreateUserDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.UserServiceImpl;
import com.teamphacode.MerchantManagement.util.annotation.ApiMessage;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {


    private final UserServiceImpl userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(UserServiceImpl userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<?> createNewUser(@Valid ReqCreatedUser userCreated, BindingResult result)
            throws IdInvalidException, MethodArgumentNotValidException {
        boolean isEmailExist = this.userService.isEmailExist(userCreated.getEmail());
        if (result.hasErrors()) {
            throw new MethodArgumentNotValidException(null, result);
        }
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + userCreated.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.handleAdminCreateUser(userCreated));
    }

//    @DeleteMapping("/users/{id}")
//    @ApiMessage("Delete a user")
//    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
//            throws IdInvalidException {
//        User currentUser = this.userService.fetchUserById(id);
//        if (currentUser == null) {
//            throw new IdInvalidException("User với id = " + id + " không tồn tại");
//        }
//
//        this.userService.handleDeleteUser(id);
//        return ResponseEntity.ok(null);
//    }

//    @GetMapping("/users/{id}")
//    @ApiMessage("fetch user by id")
//    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws IdInvalidException {
//        User fetchUser = this.userService.fetchUserById(id);
//        if (fetchUser == null) {
//            throw new IdInvalidException("User với id = " + id + " không tồn tại");
//        }
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(this.userService.convertToResUserDTO(fetchUser));
//    }

    // fetch all users
    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @Filter Specification<Users> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.fetchAllUser(spec, pageable));
    }

    @PutMapping("/users")
    @ApiMessage("Update a user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody ResupdateUser user, BindingResult result) throws IdInvalidException, MethodArgumentNotValidException {
        if(user == null){
            throw new IdInvalidException("Update user null");
        }
        if (result.hasErrors()) {
            throw new MethodArgumentNotValidException(null, result);
        }
        return ResponseEntity.ok(this.userService.handleUpdateUser(user));
    }
}
