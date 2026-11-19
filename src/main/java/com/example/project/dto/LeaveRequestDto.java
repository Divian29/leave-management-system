package com.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestDto {
    // Employee ID must be provided and greater than zero
    @NotNull(message = "Employee ID is required")
    @Positive(message = "Employee ID must be positive")
    private Long employeeId;

    // Leave start date is required
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    // Leave end date is required
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // Reason cannot be empty or contain only spaces
    @NotBlank(message = "Leave reason is required")
    private String reason;
}
