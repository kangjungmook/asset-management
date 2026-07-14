package com.company.ams.config;

import com.company.ams.entity.User;
import com.company.ams.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${ams.admin.employee-no:admin}")
    private String adminEmployeeNo;

    @Value("${ams.admin.pasz    sword:admin1234}")
    private String adminPassword;

    @Value("${ams.admin.name:관리자}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (userMapper.findByEmployeeNo(adminEmployeeNo) != null) {
            return;
        }
        User admin = new User();
        admin.setEmployeeNo(adminEmployeeNo);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setName(adminName);
        admin.setIsAdmin(true);
        admin.setIsActive(true);
        admin.setMustChangePassword(false);
        userMapper.insert(admin);
        log.info("최고관리자 계정이 생성되었습니다: {}", adminEmployeeNo);
    }
}
