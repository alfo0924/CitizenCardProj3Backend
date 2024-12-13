package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字元之間")
    private String name;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件地址")
    @Size(max = 100, message = "電子郵件長度不能超過100個字元")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "密碼必須至少8個字元，包含大小寫字母、數字和特殊字符"
    )
    private String password;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;

    @Pattern(
            regexp = "^09\\d{8}$",
            message = "請輸入有效的手機號碼格式（例如：0912345678）"
    )
    @Size(max = 20, message = "手機號碼長度不能超過20個字元")
    private String phone;

    @NotNull(message = "生日不能為空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthday;

    @NotNull(message = "性別不能為空")
    @Pattern(
            regexp = "^(MALE|FEMALE)$",
            message = "性別必須是 MALE 或 FEMALE"
    )
    private String gender;

    @Size(max = 500, message = "地址長度不能超過500個字元")
    private String address;

    private String role = "ROLE_USER";

    private boolean active = true;

    private boolean emailVerified = false;

    private Integer version = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Setter
    private String lastLoginIp;

    @Setter
    private LocalDateTime lastLoginTime;

    @Size(max = 200, message = "頭像URL長度不能超過200個字元")
    private String avatar;

    @AssertTrue(message = "密碼與確認密碼不一致")
    private boolean isPasswordMatch() {
        return password == null || confirmPassword == null
                || password.equals(confirmPassword);
    }

    @AssertTrue(message = "生日不能大於今天")
    private boolean isValidBirthday() {
        if (birthday == null) return true;
        try {
            return java.time.LocalDate.parse(birthday)
                    .isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

}
