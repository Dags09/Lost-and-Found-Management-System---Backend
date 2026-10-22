package com.lostandfound.backend.dto;

import jakarta.validation.constraints.Size;

/** Optional note from staff. Required (by the service) when rejecting. */
public record ReviewRequest(@Size(max = 500) String note) {}