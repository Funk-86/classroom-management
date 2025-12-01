package org.example.classroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.dto.ApprovalRequest;
import org.example.classroom.dto.UserReservationHistoryResponse;
import org.example.classroom.entity.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService extends IService<Reservation> {
    IPage<Reservation> getAllReservations(Integer page, Integer size, Integer status,
                                          LocalDate startDate, LocalDate endDate);

    Reservation createReservation(Reservation reservation);
    Reservation getReservationById(String id);
    List<Reservation> getUserReservations(String userId, Integer page, Integer size);
    List<Reservation> getClassroomReservations(String classroomId, LocalDate date, Integer page, Integer size);
    boolean cancelReservation(String id);
    boolean checkTimeConflict(String classroomId, LocalDate date, String startTime, String endTime);
    IPage<UserReservationHistoryResponse> getUserHistoryReservations(String userId,
                                                                     Integer page,
                                                                     Integer size);

    IPage<UserReservationHistoryResponse> getUserHistoryReservationsByDateRange(String userId,
                                                                                LocalDate startDate,
                                                                                LocalDate endDate,
                                                                                Integer page,
                                                                                Integer size);
    long countByStatus(int status);
    List<Reservation> getRecentReservations(int limit);
    List<Reservation> getRecentReservationsByStatus(Integer status, Integer limit);
    boolean approveReservation(ApprovalRequest approvalRequest, String approverId);
    IPage<Reservation> getPendingApprovals(Integer page, Integer size,
                                           LocalDate startDate, LocalDate endDate);
    List<Reservation> getTodayPendingApprovals();
    IPage<Reservation> getApprovalHistory(String approverId, Integer page, Integer size,
                                          LocalDate startDate, LocalDate endDate);

}