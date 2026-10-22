package com.lostandfound.backend.config;

import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import com.lostandfound.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class UserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String moderatorPassword;
    private final String studentPassword;

    public UserSeeder(UserRepository users, PasswordEncoder encoder,
                      @Value("${app.seed.admin-password:}") String adminPassword,
                      @Value("${app.seed.moderator-password:}") String moderatorPassword,
                      @Value("${app.seed.student-password:}") String studentPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.moderatorPassword = moderatorPassword;
        this.studentPassword = studentPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(Role.Admin,     "Admin",     "User", null,       "admin@lostandfound.local",     "09000000001", adminPassword);
        seed(Role.Moderator, "Moderator", "User", null,       "moderator@lostandfound.local", "09000000002", moderatorPassword);
        seed(Role.Student,   "Student",   "User", "20260001", "student@lostandfound.local",   "09000000003", studentPassword);
    }

    private void seed(Role role, String first, String last, String studentId,
                      String email, String phone, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            log.warn("Seed skipped for {}: no password configured", role);
            return;
        }
        if (users.existsByEmail(email) || users.existsByPhoneNum(phone)
                || (studentId != null && users.existsByStudentId(studentId))) {
            log.info("Seed skipped for {}: account already exists", role);
            return;
        }
        Users u = new Users();
        u.setFirstName(first);
        u.setLastName(last);
        u.setStudentId(studentId);
        u.setEmail(email);
        u.setPhoneNum(phone);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        users.save(u);
        log.info("Seeded {} account: {}", role, email);
    }
}