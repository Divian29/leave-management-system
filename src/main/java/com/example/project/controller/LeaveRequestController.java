package com.example.project.controller;

import com.example.project.dto.LeaveDecisionDto;
import com.example.project.dto.LeavePageResponse;
import com.example.project.dto.LeaveRequestDto;
import com.example.project.entity.LeaveRequest;
import com.example.project.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;

    // Constructor injection
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    // Create a new leave request
    @PostMapping
    public LeaveRequest createLeaveRequest(
            @Valid @RequestBody LeaveRequestDto dto) {

        // Delegate the business logic to the service
        return leaveRequestService.createLeaveRequest(dto);
    }

    // Retrieve a leave request by ID
    @GetMapping("/{id}")
    public LeaveRequest getLeaveRequest(@PathVariable Long id) {

        // Delegate the retrieval to the service
        return leaveRequestService.getLeaveRequest(id);
    }

    // Allow a manager to approve or reject a leave request
    @PatchMapping("/{id}/decision")
    public LeaveRequest decideLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDecisionDto dto) {

        // Delegate the decision logic to the service
        return leaveRequestService.decideLeaveRequest(id, dto);
    }

    // Retrieve an employee's leave requests with pagination
    @GetMapping
    public LeavePageResponse<LeaveRequest> getEmployeeLeaveRequests(
            @RequestParam Long employeeId,
            Pageable pageable) {

        return leaveRequestService.getEmployeeLeaveRequests(
                employeeId, pageable);
    }
}
