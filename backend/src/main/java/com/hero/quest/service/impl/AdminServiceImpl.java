package com.hero.quest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.quest.config.BusinessException;
import com.hero.quest.config.JwtTokenProvider;
import com.hero.quest.dto.LoginRequest;
import com.hero.quest.dto.RegisterRequest;
import com.hero.quest.entity.Admin;
import com.hero.quest.mapper.AdminMapper;
import com.hero.quest.service.AdminService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminServiceImpl(AdminMapper adminMapper, PasswordEncoder passwordEncoder,
                            JwtTokenProvider jwtTokenProvider) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        if (adminMapper.selectOne(wrapper) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建管理员
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setEmail(request.getEmail());
        adminMapper.insert(admin);

        return jwtTokenProvider.generateToken(admin.getId(), admin.getUsername());
    }

    @Override
    public String login(LoginRequest request) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return jwtTokenProvider.generateToken(admin.getId(), admin.getUsername());
    }

    @Override
    public Admin getCurrentAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Admin) {
            return (Admin) principal;
        }
        throw new BusinessException(401, "未登录");
    }
}
