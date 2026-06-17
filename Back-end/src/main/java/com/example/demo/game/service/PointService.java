package com.example.demo.game.service;

import com.example.demo.common.dto.PointEventDto;
import com.example.demo.common.sse.LivesSseManager;
import com.example.demo.game.entity.ScoreLog;
import com.example.demo.game.repository.ScoreLogRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final ScoreLogRepository scoreLogRepository;
    private final UserRepository userRepository;
    private final LivesSseManager livesSseManager;

    @Transactional
    public int addPointForApiCall(String userId, String uri) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<ScoreLog> todayLogs = scoreLogRepository.findByUserAndCreatedAtBetween(user, start, end);
        if (todayLogs.size() >= 3) {
            return 0;
        }

        int points = switch (todayLogs.size()) {
            case 0 -> 100;
            case 1 -> 130;
            case 2 -> 150;
            default -> 0;
        };

        scoreLogRepository.save(ScoreLog.builder()
                .user(user)
                .source("OPEN_API:" + uri)
                .scoreGiven(points)
                .build());

        int updated = userRepository.addScore(userId, points);
        if (updated == 0) {
            throw new IllegalStateException("Score update failed for uid=" + userId);
        }

        try {
            livesSseManager.publishPoints(userId, new PointEventDto(points, user.getScore() + points));
        } catch (Exception ignored) {
        }

        return points;
    }

    public void migrateScores() {
        userRepository.syncUserScores();
    }
}
