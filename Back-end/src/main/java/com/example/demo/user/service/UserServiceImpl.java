package com.example.demo.user.service;

import com.example.demo.user.dto.*;
import com.example.demo.user.entity.User;
import com.example.demo.school.entity.SchoolEntity;
import com.example.demo.school.repository.SchoolRepository;
import com.example.demo.user.repository.UserRepository;
import com.example.demo.device.repository.*;
import com.example.demo.game.repository.RankingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PetInputLogRepository petInputLogRepository;
    private final RankingRepository rankingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    @Transactional
    public String registerUser(UserRegisterDto dto) {
        if (dto.getSchoolId() == null || dto.getSchoolId().trim().isEmpty()) {
            return "학교를 선택해 주세요.";
        }

        Long schoolId;
        try {
            schoolId = Long.parseLong(dto.getSchoolId());
        } catch (NumberFormatException e) {
            return "올바른 학교 ID 형식이 아닙니다.";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return "비밀번호가 일치하지 않습니다.";
        }

        if (userRepository.existsByUserId(dto.getUserId())) {
            return "중복된 아이디입니다.";
        }

        SchoolEntity school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학교가 존재하지 않습니다."));

        User user = new User(
                dto.getUserId(),
                dto.getCharName(),
                dto.getImageUrl(),
                null,
                dto.getName(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone(),
                LocalDateTime.now(),
                3,
                dto.getStudentNumber(),
                school,
                null, null
        );

        userRepository.save(user);
        return "회원가입 성공";
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByUserId(dto.getUserId()).orElse(null);

        if (user == null) {
            return new LoginResponseDto("아이디가 존재하지 않습니다.", false);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return new LoginResponseDto("비밀번호가 일치하지 않습니다.", false);
        }

        int recycleCount = petInputLogRepository.countByUserId(user);
        Integer highestScore = rankingRepository.findHighestScoreByUserId(user);
        if (highestScore == null)
            highestScore = 0;

        return new LoginResponseDto(
                "로그인 성공",
                true,
                user.getUserId(),
                user.getCharName(),
                user.getTotalLives(),
                recycleCount,
                highestScore
        );
    }

    @Override
    public List<UserRankingDto> getUserRanking() {
        return null;
    }

    @Override
    public void startGameSession(StartSessionDto dto) {
    }

    @Override
    public boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
