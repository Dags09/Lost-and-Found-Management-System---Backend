package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNum(String phoneNum);
    boolean existsByStudentId(Integer studentId);
}