package org.example._citizencard3.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "名稱不能為空")
    private String name;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @NotBlank(message = "角色不能為空")
    private String role;

    private Boolean active;
}
