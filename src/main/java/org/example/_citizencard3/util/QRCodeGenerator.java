package org.example._citizencard3.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class QRCodeGenerator {

    private static final int QR_CODE_SIZE = 200;
    private static final String IMAGE_FORMAT = "PNG";
    @Value("${app.ticket.encryption.key}")
    private String encryptionKey;

    public String generateQRCodeData(String prefix, Long id, LocalDateTime validUntil) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s-%s-%06d-%s-%s",
                prefix,
                timestamp,
                id,
                uniqueId,
                validUntil.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    public String generateQRCodeImage(String qrCodeData) throws IOException {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrCodeData,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE,
                    hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (WriterException e) {
            throw new IOException("QR碼生成失敗", e);
        }
    }

    public boolean validateQRCodeData(String qrCodeData, String prefix) {
        try {
            String[] parts = qrCodeData.split("-");
            if (parts.length != 5) {
                return false;
            }

            // 驗證前綴
            if (!parts[0].equals(prefix)) {
                return false;
            }

            // 驗證時間戳格式
            LocalDateTime.parse(parts[1], DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 驗證ID格式
            Long.parseLong(parts[2]);

            // 驗證UUID部分長度
            if (parts[3].length() != 8) {
                return false;
            }

            // 驗證有效期限格式
            LocalDateTime validUntil = LocalDateTime.parse(parts[4], DateTimeFormatter.ofPattern("yyyyMMdd"));
            return validUntil.isAfter(LocalDateTime.now());

        } catch (Exception e) {
            return false;
        }
    }

    public String extractIdFromQRCode(String qrCodeData) {
        try {
            String[] parts = qrCodeData.split("-");
            if (parts.length >= 3) {
                return parts[2];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 新增電影票加密資訊生成方法
    public String generateMovieTicketQRCodeData(Long ticketId, String movieName,
        String showTime, String hallName, String seatNumber, LocalDateTime validUntil) {
        try {
            // 組織票券資訊
            Map<String, String> ticketInfo = new HashMap<>();
            ticketInfo.put("ticketId", ticketId.toString());
            ticketInfo.put("movieName", movieName);
            ticketInfo.put("showTime", showTime);
            ticketInfo.put("hallName", hallName);
            ticketInfo.put("seatNumber", seatNumber);
            ticketInfo.put("validUntil", validUntil.toString());

            // 轉換為JSON並加密
            String jsonData = new ObjectMapper().writeValueAsString(ticketInfo);
            return encryptTicketData(jsonData);
        } catch (Exception e) {
            throw new RuntimeException("電影票QR碼生成失敗", e);
        }
    }

    // 加密方法
    private String encryptTicketData(String data) throws Exception {
        // 明確指定UTF-8編碼和加密模式
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        // 使用 ECB 模式和 PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // 解密方法
    public String decryptTicketData(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
