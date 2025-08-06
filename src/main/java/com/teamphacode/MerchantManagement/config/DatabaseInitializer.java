package com.teamphacode.MerchantManagement.config;

import com.teamphacode.MerchantManagement.domain.Users;
import com.teamphacode.MerchantManagement.repository.UserRepository;
import com.teamphacode.MerchantManagement.util.constant.GenderEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countUsers = this.userRepository.count();
        if (countUsers == 0) {
            Users system = new Users();
            system.setEmail("client@gmail.com");
            system.setAddress("Hà Nội");
            system.setAge(25);
            system.setGender(GenderEnum.MALE);
            system.setName("I'm super client");
            system.setPassword(this.passwordEncoder.encode("123456"));
            this.userRepository.save(system);
        }

        if (countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else
            System.out.println(">>> END INIT DATABASE");
    }

}