package com.example.demo.device.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.sse.LivesSseManager;
import com.example.demo.user.dto.LivesDto;
import com.example.demo.device.dto.PetInputLogDto;
import com.example.demo.device.entity.Device;
import com.example.demo.device.entity.PetInputLog;
import com.example.demo.device.repository.DeviceRepository;
import com.example.demo.device.repository.PetInputLogRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetInputLogService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PetInputLogRepository petInputLogRepository;
    private final LivesSseManager sse;

    @Transactional
    public String saveInputLog(PetInputLogDto dto) {
        User user = null;
        if (dto.getStudentNumber() != null && !dto.getStudentNumber().isBlank()) {
            user = userRepository.findByStudentNumber(dto.getStudentNumber()).orElse(null);
        }

        Device device = deviceRepository.findById(dto.getDeviceId()).orElse(null);
        if (device == null) {
            return "등록되지 않은 디바이스입니다";
        }

        if (dto.getInputCount() <= 0) {
            return "정상 PET이 아니므로 저장하지 않음";
        }

        PetInputLog log = PetInputLog.builder()
                .userId(user)
                .school(device.getSchool())
                .device(device)
                .studentNumber(dto.getStudentNumber())
                .inputCount(dto.getInputCount())
                .inputTime(dto.getInputTime() != null ? dto.getInputTime() : LocalDateTime.now())
                .build();

        petInputLogRepository.save(log);

        if (user != null) {
            user.setTotalLives(user.getTotalLives() + dto.getInputCount());
            userRepository.save(user);

            Integer total = petInputLogRepository.getTotalCountByUserId(user.getUserId());
            int totalRecycleCount = (total != null) ? total : 0;

            sse.publishLives(
                user.getUserId(),
                new LivesDto(
                    user.getUserId(),
                    user.getTotalLives(),
                    totalRecycleCount,
                    LocalDateTime.now(),
                    dto.getInputCount())
            );
        }

        return "success";
    }

    public List<PetInputLogDto> getLogsByUserId(String userId) {
        List<PetInputLog> logs = petInputLogRepository.findTop50ByUserId_UserIdOrderByInputTimeDesc(userId);

        return logs.stream().map(log -> {
            PetInputLogDto dto = new PetInputLogDto();
            dto.setUserId(log.getUserId().getUserId());
            dto.setDeviceId(log.getDevice().getDeviceId());
            dto.setInputCount(log.getInputCount());
            dto.setInputTime(log.getInputTime());
            return dto;
        }).collect(Collectors.toList());
    }
}
