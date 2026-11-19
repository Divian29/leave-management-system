package com.example.project.dto;

import com.example.project.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveDecisionDto {

    // A manager must provide a decision
    @NotNull(message = "Decision status is required")
    private LeaveStatus status;
}
