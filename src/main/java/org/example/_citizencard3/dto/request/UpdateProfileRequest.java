package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字元之間")
    private String name;

    @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼")
    @Size(max = 20, message = "手機號碼長度不能超過20個字元")
    private String phone;

    @Size(max = 10, message = "生日格式不正確")
    private String birthday;

    @Size(max = 10, message = "性別長度不能超過10個字元")
    private String gender;

    @Size(max = 500, message = "地址長度不能超過500個字元")
    private String address;

    @Size(max = 200, message = "頭像URL長度不能超過200個字元")
    private String avatar;
}
