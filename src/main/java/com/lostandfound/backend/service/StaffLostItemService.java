package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.PageResponse;
import com.lostandfound.backend.dto.StaffLostItemResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.Users;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.repository.ItemRepository;
import com.lostandfound.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/** Staff (Moderator or Admin) can read lost reports, so they can help a student who comes asking. */
@Service
public class StaffLostItemService {

    private final ItemRepository items;
    private final UserRepository users;

    public StaffLostItemService(ItemRepository items, UserRepository users) {
        this.items = items;
        this.users = users;
    }

    /**
     * Lost reports with the given status, newest first. q narrows by words in the name, description,
     * colour or brand. Reporters are loaded in one query for the whole page.
     */
    @Transactional(readOnly = true)
    public PageResponse<StaffLostItemResponse> list(ItemStatus status, String q, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());

        Page<Items> result = (q == null || q.isBlank())
                ? items.findByTypeAndStatus(ItemType.LOST, status, pageable)
                : items.searchByTypeAndStatus(ItemType.LOST, status, likePattern(q), pageable);

        // one lookup for all the reporters on this page, not one per report
        Map<String, Users> reporters = users.findAllById(
                        result.getContent().stream().map(Items::getReporterId)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Users::getId, Function.identity()));

        List<StaffLostItemResponse> content = result.getContent().stream()
                .map(i -> StaffLostItemResponse.from(i, reporters.get(i.getReporterId())))
                .toList();
        return new PageResponse<>(content, safePage, safeSize,
                result.getTotalElements(), result.getTotalPages());
    }

    /** One lost report with the reporter's contact details. Found items are not served here (404). */
    @Transactional(readOnly = true)
    public StaffLostItemResponse get(String id) {
        Items i = items.findById(id)
                .filter(x -> x.getType() == ItemType.LOST)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lost report not found"));
        Users reporter = i.getReporterId() == null ? null : users.findById(i.getReporterId()).orElse(null);
        return StaffLostItemResponse.from(i, reporter);
    }

    /** "Black Nike" -> "%black nike%", with % and _ typed by the user treated as plain characters. */
    static String likePattern(String q) {
        String safe = q.trim().toLowerCase()
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + safe + "%";
    }
}