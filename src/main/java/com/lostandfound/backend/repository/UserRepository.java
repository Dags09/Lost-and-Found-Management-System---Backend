package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Database access for user accounts. */
public interface UserRepository extends JpaRepository<Users, String> {
    /** Emails are stored lowercase, so callers lowercase before searching. */
    Optional<Users> findByEmail(String email);
    /** Duplicate checks used by sign-up and staff creation. */
    boolean existsByEmail(String email);
    boolean existsByPhoneNum(String phoneNum);
    boolean existsByStudentId(String studentId);

    /** Users with one role, one page at a time (Admin user list). */
    Page<Users> findByRole(Role role, Pageable pageable);

    /**
     * Reads only the role column. JwtAuthFilter calls this on every request, so a role change or
     * deleted account takes effect immediately.
     */
    @Query("select u.role from Users u where u.id = :id")
    Optional<Role> findRoleById(@Param("id") String id);
}