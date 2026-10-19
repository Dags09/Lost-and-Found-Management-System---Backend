package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNum(String phoneNum);
    boolean existsByStudentId(Integer studentId);

    @Query("select u.role from Users u where u.id = :id")
    Optional<Role> findRoleById(@Param("id") String id);
}