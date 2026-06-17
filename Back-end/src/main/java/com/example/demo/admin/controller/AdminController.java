package com.example.demo.admin.controller;

import com.example.demo.admin.dto.AdminInfoResponseDto;
import com.example.demo.admin.dto.AdminInfoUpdateRequestDto;
import com.example.demo.admin.dto.AdminLoginRequestDto;
import com.example.demo.admin.dto.AdminLoginResponseDto;
import com.example.demo.admin.dto.NotificationResponseDto;
import com.example.demo.admin.dto.SchoolStatusResponse;
import com.example.demo.admin.dto.PasswordChangeRequestDto;
import com.example.demo.admin.service.AdminService;
import com.example.demo.device.entity.Device;
import com.example.demo.device.entity.DeviceCheckLog;
import com.example.demo.device.repository.DeviceRepository;
import com.example.demo.school.entity.SchoolEntity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final DeviceRepository deviceRepository;

    public AdminController(AdminService adminService, DeviceRepository deviceRepository) {
        this.adminService = adminService;
        this.deviceRepository = deviceRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequestDto dto) {
        AdminLoginResponseDto result = adminService.login(dto);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }

    @GetMapping("/schools")
    public ResponseEntity<List<SchoolStatusResponse>> getSchoolsByRegion(@RequestParam Long adminId) {
        List<SchoolEntity> schools = adminService.getSchoolsByAdminRegion(adminId);

        List<SchoolStatusResponse> response = schools.stream().map(school -> {
            List<Device> devices = deviceRepository.findBySchool(school);
            Device device = devices.isEmpty() ? null : devices.get(0);

            double loadRate = (device != null) ? device.getCapacity() : 0.0;
            Long deviceId = (device != null) ? device.getDeviceId() : null;

            return new SchoolStatusResponse(
                school.getSchoolName(),
                school.getAddress(),
                loadRate,
                deviceId
            );
        }).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequestDto request) {
        boolean result = adminService.changePassword(
            request.getAdminId(),
            request.getCurrentPassword(),
            request.getNewPassword()
        );

        if (result) {
            return ResponseEntity.ok("비밀번호 변경 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    @PutMapping("/{adminId}/info")
    public ResponseEntity<String> updateAdminInfo(
            @PathVariable Long adminId,
            @RequestBody AdminInfoUpdateRequestDto dto) {

        dto.setAdminId(adminId);
        adminService.updateAdminInfo(dto);
        return ResponseEntity.ok("관리자 정보가 변경되었습니다.");
    }

    @GetMapping("/{adminId}/info")
    public ResponseEntity<AdminInfoResponseDto> getAdminInfo(@PathVariable Long adminId) {
        return adminService.getAdminInfo(adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 알림 조회 (DeviceCheckLog → DTO 변환)
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(@RequestParam Long adminId) {
        List<DeviceCheckLog> logs = adminService.getNotifications(adminId);

        // DeviceCheckLog → NotificationResponseDto 변환
        List<NotificationResponseDto> response = logs.stream().map(log -> {
            String schoolName = log.getDeviceId().getSchool() != null
                    ? log.getDeviceId().getSchool().getSchoolName()
                    : "알 수 없음";

            return new NotificationResponseDto(
                log.getCheckLogId(),
                log.getAdminId().getAdmName(),
                log.getActionType(),
                log.getLogTime(),
                log.getDeviceId().getDeviceId(),
                schoolName
            );
        }).toList();

        return ResponseEntity.ok(response);
    }
}
