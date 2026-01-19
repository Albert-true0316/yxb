package com.hero.quest.controller;

import com.hero.quest.dto.ApiResponse;
import com.hero.quest.dto.LoginRequest;
import com.hero.quest.dto.RegisterRequest;
import com.hero.quest.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminService adminService;

    public AuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String token = adminService.register(request);
        return ApiResponse.success("注册成功", Map.of("token", token));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = adminService.login(request);
        return ApiResponse.success("登录成功", Map.of("token", token));
    }
}
