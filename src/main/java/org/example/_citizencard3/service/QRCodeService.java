package org.example._citizencard3.service;

import org.example._citizencard3.model.MovieTicketQRCode;
import org.example._citizencard3.model.DiscountCouponQRCode;
import org.example._citizencard3.repository.MovieTicketQRCodeRepository;
import org.example._citizencard3.repository.DiscountCouponQRCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class QRCodeService {

    @Autowired
    private MovieTicketQRCodeRepository movieTicketQRCodeRepository;

    @Autowired
    private DiscountCouponQRCodeRepository discountCouponQRCodeRepository;

    // 生成電影票QR碼
    @Transactional
    public MovieTicketQRCode generateMovieTicketQRCode(Long ticketId, LocalDateTime validUntil) {
        String qrCodeData = generateQRCodeData("TIX", ticketId);
        String qrCodeUrl = generateQRCodeImage(qrCodeData);

        MovieTicketQRCode qrCode = new MovieTicketQRCode();
        qrCode.setTicketId(ticketId);
        qrCode.setQrCodeData(qrCodeData);
        qrCode.setQrCodeUrl(qrCodeUrl);
        qrCode.setValidUntil(validUntil);
        qrCode.setIsUsed(false);

        return movieTicketQRCodeRepository.save(qrCode);
    }

    // 生成優惠券QR碼
    @Transactional
    public DiscountCouponQRCode generateDiscountCouponQRCode(Long couponId, LocalDateTime validUntil) {
        String qrCodeData = generateQRCodeData("CPN", couponId);
        String qrCodeUrl = generateQRCodeImage(qrCodeData);

        DiscountCouponQRCode qrCode = new DiscountCouponQRCode();
        qrCode.setCouponId(couponId);
        qrCode.setQrCodeData(qrCodeData);
        qrCode.setQrCodeUrl(qrCodeUrl);
        qrCode.setValidUntil(validUntil);
        qrCode.setIsUsed(false);

        return discountCouponQRCodeRepository.save(qrCode);
    }

    // 驗證電影票QR碼
    @Transactional
    public boolean validateMovieTicketQRCode(String qrCodeData, Long ticketId) {
        Optional<MovieTicketQRCode> qrCode = movieTicketQRCodeRepository.findByQrCodeData(qrCodeData);

        if (qrCode.isPresent()) {
            MovieTicketQRCode code = qrCode.get();
            return !code.getIsUsed() && code.getValidUntil().isAfter(LocalDateTime.now());
        }
        return false;
    }

    // 驗證優惠券QR碼
    @Transactional
    public boolean validateDiscountCouponQRCode(String qrCodeData, Long couponId) {
        Optional<DiscountCouponQRCode> qrCode = discountCouponQRCodeRepository.findByQrCodeData(qrCodeData);

        if (qrCode.isPresent()) {
            DiscountCouponQRCode code = qrCode.get();
            return !code.getIsUsed() && code.getValidUntil().isAfter(LocalDateTime.now());
        }
        return false;
    }

    // 標記電影票QR碼已使用
    @Transactional
    public void markMovieTicketQRCodeAsUsed(String qrCodeData) {
        Optional<MovieTicketQRCode> qrCode = movieTicketQRCodeRepository.findByQrCodeData(qrCodeData);
        qrCode.ifPresent(code -> {
            code.setIsUsed(true);
            code.setUsedAt(LocalDateTime.now());
            movieTicketQRCodeRepository.save(code);
        });
    }

    // 標記優惠券QR碼已使用
    @Transactional
    public void markDiscountCouponQRCodeAsUsed(String qrCodeData) {
        Optional<DiscountCouponQRCode> qrCode = discountCouponQRCodeRepository.findByQrCodeData(qrCodeData);
        qrCode.ifPresent(code -> {
            code.setIsUsed(true);
            code.setUsedAt(LocalDateTime.now());
            discountCouponQRCodeRepository.save(code);
        });
    }

    // 生成QR碼數據
    private String generateQRCodeData(String prefix, Long id) {
        return prefix + "-" + LocalDateTime.now().getYear() + "-" +
                String.format("%06d", id) + "-" +
                UUID.randomUUID().toString().substring(0, 8);
    }

    // 生成QR碼圖片
    private String generateQRCodeImage(String qrCodeData) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR碼生成失敗", e);
        }
    }
    // 在QRCodeService.java中添加以下方法

    // 獲取電影票QR碼
    @Transactional(readOnly = true)
    public MovieTicketQRCode getMovieTicketQRCode(Long ticketId) {
        Optional<MovieTicketQRCode> qrCode = movieTicketQRCodeRepository.findByTicketId(ticketId);
        if (!qrCode.isPresent()) {
            throw new RuntimeException("找不到對應的電影票QR碼");
        }
        return qrCode.get();
    }

    // 獲取優惠券QR碼
    @Transactional(readOnly = true)
    public DiscountCouponQRCode getDiscountCouponQRCode(Long couponId) {
        Optional<DiscountCouponQRCode> qrCode = discountCouponQRCodeRepository.findByCouponId(couponId);
        if (!qrCode.isPresent()) {
            throw new RuntimeException("找不到對應的優惠券QR碼");
        }
        return qrCode.get();
    }

    // 重新生成電影票QR碼
    @Transactional
    public MovieTicketQRCode regenerateMovieTicketQRCode(Long ticketId) {
        Optional<MovieTicketQRCode> existingQRCode = movieTicketQRCodeRepository.findByTicketId(ticketId);
        if (!existingQRCode.isPresent()) {
            throw new RuntimeException("找不到對應的電影票QR碼");
        }

        MovieTicketQRCode qrCode = existingQRCode.get();
        String newQRCodeData = generateQRCodeData("TIX", ticketId);
        String newQRCodeUrl = generateQRCodeImage(newQRCodeData);

        qrCode.setQrCodeData(newQRCodeData);
        qrCode.setQrCodeUrl(newQRCodeUrl);
        qrCode.setIsUsed(false);
        qrCode.setUsedAt(null);

        return movieTicketQRCodeRepository.save(qrCode);
    }

    // 重新生成優惠券QR碼
    @Transactional
    public DiscountCouponQRCode regenerateDiscountCouponQRCode(Long couponId) {
        Optional<DiscountCouponQRCode> existingQRCode = discountCouponQRCodeRepository.findByCouponId(couponId);
        if (!existingQRCode.isPresent()) {
            throw new RuntimeException("找不到對應的優惠券QR碼");
        }

        DiscountCouponQRCode qrCode = existingQRCode.get();
        String newQRCodeData = generateQRCodeData("CPN", couponId);
        String newQRCodeUrl = generateQRCodeImage(newQRCodeData);

        qrCode.setQrCodeData(newQRCodeData);
        qrCode.setQrCodeUrl(newQRCodeUrl);
        qrCode.setIsUsed(false);
        qrCode.setUsedAt(null);

        return discountCouponQRCodeRepository.save(qrCode);
    }

}
