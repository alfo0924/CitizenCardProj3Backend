package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "姓名不能為空")
    private String name;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼長度至少為6個字符")
    private String password;

    @NotBlank(message = "角色不能為空")
    private String role;
}
