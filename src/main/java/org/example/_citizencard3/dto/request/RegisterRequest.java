package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字元之間")
    private String name;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件地址")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "密碼必須至少8個字元，包含至少一個字母和一個數字"
    )
    private String password;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;

    @Pattern(
            regexp = "^09\\d{8}$",
            message = "請輸入有效的手機號碼"
    )
    private String phone;

    @NotNull(message = "生日不能為空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthday;

    @NotNull(message = "性別不能為空")
    @Pattern(
            regexp = "^(MALE|FEMALE|OTHER)$",
            message = "性別必須是 MALE、FEMALE 或 OTHER"
    )
    private String gender;

    @Size(max = 200, message = "地址長度不能超過200個字元")
    private String address;
}
