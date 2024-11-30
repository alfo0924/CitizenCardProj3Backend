package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2-50個字元之間")
    private String name;

    @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼")
    private String phone;

    private String birthday;

    private String gender;

    @Size(max = 200, message = "地址長度不能超過200個字元")
    private String address;
}