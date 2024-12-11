package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeValidateRequest {

    @NotBlank(message = "QR碼數據不能為空")
    private String qrCodeData;

    @NotNull(message = "QR碼類型不能為空")
    private QRCodeType type;

    @NotNull(message = "ID不能為空")
    private Long id;

    public enum QRCodeType {
        MOVIE_TICKET,
        DISCOUNT_COUPON
    }
}
