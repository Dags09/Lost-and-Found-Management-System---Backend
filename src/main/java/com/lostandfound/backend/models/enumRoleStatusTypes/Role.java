package com.lostandfound.backend.models.enumRoleStatusTypes;

/**
 * Account roles. The spelling matters: Spring turns them into ROLE_Student, ROLE_Moderator and
 * ROLE_Admin, which SecurityConfig and @PreAuthorize check.
 */
public enum Role {
    Student, Moderator, Admin
}
