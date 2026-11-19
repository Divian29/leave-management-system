package com.example.project.service;

import com.example.project.dto.LeaveDecisionDto;
import com.example.project.dto.LeavePageResponse;
import com.example.project.dto.LeaveRequestDto;
import com.example.project.entity.LeaveRequest;
import com.example.project.entity.LeaveStatus;
import com.example.project.repository.LeaveRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

@Service
public class LeaveRequestService {
    private final LeaveRequestRepository repository;

    private static final int ANNUAL_LEAVE_ALLOWANCE = 20;  //We define the annual allowance once rather than
    // repeatedly writing 20 throughout our code

    public LeaveRequestService(LeaveRequestRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequestDto dto) {

        // Get the current date
        LocalDate today = LocalDate.now();

        // 1. Validate that the start date is not after the end date
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        // 2. Prevent employees from requesting leave for past dates
        if (dto.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "Leave cannot be requested for past dates"
            );
        }

        // 3. Calculate the total working days, excluding weekends
        int leaveDays = calculateWorkingDays(
                dto.getStartDate(),
                dto.getEndDate()
        );

        // Ensure the request contains at least one working day
        if (leaveDays <= 0) {
            throw new IllegalArgumentException(
                    "Leave request must contain at least one working day"
            );
        }

        // 4. Check if the employee already has overlapping leave
        // Only PENDING and APPROVED requests are considered
        boolean overlapping = repository
                .existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        dto.getEmployeeId(),
                        Arrays.asList(
                                LeaveStatus.PENDING,
                                LeaveStatus.APPROVED
                        ),
                        dto.getEndDate(),
                        dto.getStartDate()
                );

        if (overlapping) {
            throw new IllegalArgumentException(
                    "Employee already has an overlapping leave request"
            );
        }

        // 5. Check the employee's annual leave balance

// Get the calendar year of the requested leave
        int year = dto.getStartDate().getYear();

// Define the beginning and end of that year
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

// Only pending and approved requests reserve leave days
        int reservedDays = repository.sumReservedLeaveDays(
                dto.getEmployeeId(),
                Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                yearStart,
                yearEnd
        );

// Calculate the employee's remaining leave balance
        int remainingDays = ANNUAL_LEAVE_ALLOWANCE - reservedDays;

// Reject the request if it exceeds the available balance
        if (leaveDays > remainingDays) {
            throw new IllegalArgumentException(
                    "Insufficient leave balance. Remaining days: " + remainingDays
            );
        }

        // 5. Create a new leave request entity
        LeaveRequest request = new LeaveRequest();

        // Map the incoming DTO data to the entity
        request.setEmployeeId(dto.getEmployeeId());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setLeaveDays(leaveDays);
        request.setReason(dto.getReason());

        // Every new leave request starts with PENDING status
        request.setStatus(LeaveStatus.PENDING);

        // 6. Save the leave request to the database
        return repository.save(request);
    }

    // Calculate leave days, excluding weekends
    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {

        int workingDays = 0;

        // Start checking from the requested start date
        LocalDate currentDate = startDate;

        // Continue until the end date is included
        while (!currentDate.isAfter(endDate)) {

            DayOfWeek day = currentDate.getDayOfWeek();

            // Count only Monday to Friday
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }

            // Move to the next calendar day
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    // Allow a manager to approve or reject a leave request
    @Transactional
    public LeaveRequest decideLeaveRequest(Long id, LeaveDecisionDto dto) {

        // Find the leave request or report that it does not exist
        LeaveRequest request = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Leave request not found")
                );

        // Only pending requests can receive a decision
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending leave requests can be decided"
            );
        }

        // Validate the manager's decision
        if (dto.getStatus() != LeaveStatus.APPROVED
                && dto.getStatus() != LeaveStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Decision must be APPROVED or REJECTED"
            );
        }

        // Update the request status
        request.setStatus(dto.getStatus());

        // Save and return the updated request
        return repository.save(request);
    }

    // Retrieve a leave request using its ID
    public LeaveRequest getLeaveRequest(Long id) {

        // Search the database and return the request if found
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Leave request not found")
                );
    }

    // Retrieve an employee's leave requests using pagination
    public LeavePageResponse<LeaveRequest> getEmployeeLeaveRequests(
            Long employeeId, Pageable pageable) {

        // Retrieve the employee's leave requests with pagination
        Page<LeaveRequest> leavePage =
                repository.findByEmployeeId(employeeId, pageable);

        // Create our custom response
        LeavePageResponse<LeaveRequest> response = new LeavePageResponse<>();

        response.setContent(leavePage.getContent());
        response.setCurrentPage(leavePage.getNumber());
        response.setPageSize(leavePage.getSize());
        response.setTotalElements(leavePage.getTotalElements());
        response.setTotalPages(leavePage.getTotalPages());
        response.setFirst(leavePage.isFirst());
        response.setLast(leavePage.isLast());
        response.setEmpty(leavePage.isEmpty());

        return response;
    }

}
