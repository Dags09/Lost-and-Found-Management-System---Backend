package com.lostandfound.backend.dto;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


/**
 * A lost report as staff see it: the private distinguishing marks plus the reporter's contact
 * details, so staff can help the student who comes asking.
 */
public record StaffLostItemResponse(
        String id,
        String name,
        String description,
        String categoryId,
        String color,
        String brand,
        String distinguishingMarks,
        String locationName,
        LocalDate dateOccurred,
        ItemStatus status,
        Instant createdAt,
        List<ItemImageResponse> images,
        Reporter reporter
) {
    public record Reporter(String id, String name, String studentId, String email, String phoneNum) {
        static Reporter from(Users u) {
            return u == null ? null : new Reporter(u.getId(), u.getFirstName() + " " + u.getLastName(),
                    u.getStudentId(), u.getEmail(), u.getPhoneNum());
        }
    }

    public static StaffLostItemResponse from(Items i, Users reporter) {
        return new StaffLostItemResponse(
                i.getId(), i.getName(), i.getDescription(), i.getCategoryId(), i.getColor(), i.getBrand(),
                i.getDistinguishingMarks(), i.getLocationName(), i.getDateOccurred(), i.getStatus(),
                i.getCreatedAt(), ItemImageResponse.listFrom(i), Reporter.from(reporter));
    }
}