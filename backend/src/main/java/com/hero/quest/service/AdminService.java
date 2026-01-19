package com.hero.quest.service;

import com.hero.quest.dto.LoginRequest;
import com.hero.quest.dto.RegisterRequest;
import com.hero.quest.entity.Admin;

public interface AdminService {

    String register(RegisterRequest request);

    String login(LoginRequest request);

    Admin getCurrentAdmin();
}
