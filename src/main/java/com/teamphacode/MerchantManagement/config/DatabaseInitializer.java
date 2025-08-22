package com.teamphacode.MerchantManagement.config;

import com.teamphacode.MerchantManagement.domain.Permission;
import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.repository.PermissionRepository;
import com.teamphacode.MerchantManagement.repository.RoleRepository;
import com.teamphacode.MerchantManagement.repository.UserRepository;
import com.teamphacode.MerchantManagement.util.constant.GenderEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> arr = new ArrayList<>();
            ArrayList<Permission> arr1 = new ArrayList<>();
            // ----- PERMISSIONS -----
            arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET", "PERMISSIONS"));

            // ----- ROLES -----
            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "ROLES"));

            // ----- USERS -----
            arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET", "USERS"));

            // ----- MERCHANT -----
            arr.add(new Permission("Get merchants", "/api/v1/merchants", "GET", "MERCHANT"));
            arr.add(new Permission("Create merchant", "/api/v1/companies", "POST", "MERCHANT"));
            arr.add(new Permission("Update merchant", "/api/v1/merchant/update", "PUT", "MERCHANT"));
            arr.add(new Permission("Delete merchant", "/api/v1/companies/{id}", "DELETE", "MERCHANT"));

            // ----- MERCHANT_EXPORT -----
            arr.add(new Permission("Summary transaction by merchant", "/api/v1/merchants/summary-transaction-by-merchant", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Search merchant", "/api/v1/merchants/search", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Report by status", "/api/v1/merchants/report-by-status", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Fetch transaction", "/api/v1/merchants/fetch-transaction/{merchantId}", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Export transaction summary", "/api/v1/merchants/export-transactionSummary", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Export transaction detail", "/api/v1/merchants/export-transactionDetail/{merchantId}", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Export merchant year", "/api/v1/merchants/export-merchant-year", "GET", "MERCHANT_EXPORT"));
            arr.add(new Permission("Count merchant by year", "/api/v1/merchants/count-merchant-by-year", "GET", "MERCHANT_EXPORT"));

            // Mcc
            arr.add(new Permission("Get all MCC", "/api/v1/mcc/getAllMcc", "GET", "MCC"));
            arr.add(new Permission("Create MCC", "/api/v1/mcc/createMcc", "POST", "MCC"));
            arr.add(new Permission("Update MCC", "/api/v1/mcc/updateMcc/{code}", "PUT", "MCC"));
            arr.add(new Permission("Delete MCC", "/api/v1/mcc/removeMcc/{code}", "DELETE", "MCC"));

            // ----- MERCHANT_HISTORY -----
            arr.add(new Permission("Get merchant history paginate", "/api/v1/merchant-histories", "GET", "MERCHANT_HISTORY"));


            this.permissionRepository.saveAll(arr);
        }

        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);

            this.roleRepository.save(adminRole);
        }

        if (countUsers == 0) {
            Users adminUser = new Users();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("hn");
            adminUser.setAge(25);
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("I'm super admin");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            this.userRepository.save(adminUser);
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else
            System.out.println(">>> END INIT DATABASE");
    }
}