package com.example.demo.user.controller;

import com.example.demo.user.dto.*;
import com.example.demo.user.repository.UserRepository;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.LifeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LifeService lifeService;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserRegisterDto dto) {
        return userService.registerUser(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserRankingDto>> getRanking() {
        return ResponseEntity.ok(userService.getUserRanking());
    }

    @PostMapping("/session/start")
    public ResponseEntity<String> startSession(@RequestBody StartSessionDto dto) {
        userService.startGameSession(dto);
        return ResponseEntity.ok("게임 세션 시작됨");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhoneNumber(@RequestBody PhoneVerifyDto dto) {
        String phone = dto.getPhoneNumber();
        if (phone == null || !phone.matches("^010\\d{7,8}$")) {
            return ResponseEntity.badRequest().body("유효하지 않은 휴대폰 번호 형식입니다.");
        }
        return ResponseEntity.ok("휴대폰 번호 인증 성공");
    }

    @PostMapping("/check-id")
    public ResponseEntity<?> checkId(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("userId 누락됨");
        }
        boolean exists = userService.checkUserIdDuplicate(userId);
        if (exists) {
            return ResponseEntity.ok("이미 존재하는 아이디입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        }
    }

    @GetMapping("/check-student")
    public ResponseEntity<String> checkStudent(@RequestParam String studentNumber) {
        return ResponseEntity.ok("재활용을 시작합니다");
    }

    @GetMapping("/lives")
    public ResponseEntity<Integer> getLives(@RequestParam String userId) {
        return ResponseEntity.ok(lifeService.currentLives(userId));
    }

    @PostMapping("/lives/consume")
    public ResponseEntity<Integer> consumeLife(@RequestParam String userId) {
        try {
            int remaining = lifeService.consumeOne(userId);
            return ResponseEntity.ok(remaining);
        } catch (IllegalStateException e) {
            if ("NO_LIFE".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(0);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1);
        }
    }
}
