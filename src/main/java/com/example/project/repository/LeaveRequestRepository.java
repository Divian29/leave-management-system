package com.example.project.repository;

import com.example.project.entity.LeaveRequest;
import com.example.project.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // checks whether an employee already has a leave request that overlaps with
    // the new leave request they're trying to submit.
    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            Collection<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );

    // this method is to calculate how many leave days an employee has already reserved for the year.
    @Query("""
        SELECT COALESCE(SUM(l.leaveDays), 0)
        FROM LeaveRequest l
        WHERE l.employeeId = :employeeId
        AND l.status IN :statuses
        AND l.startDate BETWEEN :yearStart AND :yearEnd
        """)
    int sumReservedLeaveDays(
            @Param("employeeId") Long employeeId,
            @Param("statuses") Collection<LeaveStatus> statuses,
            @Param("yearStart") LocalDate yearStart,
            @Param("yearEnd") LocalDate yearEnd
    );

    // This method retrieves all leave requests belonging to a particular employee,
    // but it supports pagination.
    Page<LeaveRequest> findByEmployeeId(
            Long employeeId,
            Pageable pageable
    );
}
