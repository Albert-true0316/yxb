package com.hero.quest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailConfigRequest {

    @NotBlank(message = "邮箱账号不能为空")
    @Email(message = "邮箱格式不正确")
    private String mailUsername;

    @NotBlank(message = "邮箱密码不能为空")
    private String mailPassword;
}
