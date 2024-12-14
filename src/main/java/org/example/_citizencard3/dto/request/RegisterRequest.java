package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

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
    @Size(min = 8, max = 255, message = "密碼長度必須在8-255個字元之間")
    private String password;

    @Pattern(regexp = "^(|09\\d{8})$", message = "請輸入有效的手機號碼格式（例如：0912345678）或留空")
    @Size(max = 20, message = "手機號碼長度不能超過20個字元")
    private String phone;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式必須為 YYYY-MM-DD")
    @Size(max = 10, message = "生日長度不能超過10個字元")
    private String birthday;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性別必須是 MALE 或 FEMALE")
    @Size(max = 10, message = "性別長度不能超過10個字元")
    private String gender;

    @Size(max = 500, message = "地址長度不能超過500個字元")
    private String address;

    @Size(max = 200, message = "頭像URL長度不能超過200個字元")
    private String avatar;

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
