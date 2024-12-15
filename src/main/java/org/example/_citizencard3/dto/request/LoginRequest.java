package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件格式")
    @Size(max = 100, message = "電子郵件長度不能超過100個字元")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(max = 255, message = "密碼長度不能超過255個字元")
    private String password;
    @Setter
    private String ipAddress;

    private String lastLoginIp;


}