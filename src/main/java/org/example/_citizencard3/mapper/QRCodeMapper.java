package org.example._citizencard3.mapper;

import org.example._citizencard3.model.MovieTicketQRCode;
import org.example._citizencard3.model.DiscountCouponQRCode;
import org.example._citizencard3.dto.response.QRCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class QRCodeMapper {

    public QRCodeResponse toMovieTicketQRCodeResponse(MovieTicketQRCode qrCode) {
        QRCodeResponse response = new QRCodeResponse();
        response.setId(qrCode.getId());
        response.setQrCodeData(qrCode.getQrCodeData());
        response.setQrCodeUrl(qrCode.getQrCodeUrl());
        response.setValidUntil(qrCode.getValidUntil());
        response.setIsUsed(qrCode.getIsUsed());
        response.setUsedAt(qrCode.getUsedAt());
        response.setCreatedAt(qrCode.getCreatedAt());
        response.setType("MOVIE_TICKET");
        response.setRelatedId(qrCode.getTicketId());
        return response;
    }

    public QRCodeResponse toDiscountCouponQRCodeResponse(DiscountCouponQRCode qrCode) {
        QRCodeResponse response = new QRCodeResponse();
        response.setId(qrCode.getId());
        response.setQrCodeData(qrCode.getQrCodeData());
        response.setQrCodeUrl(qrCode.getQrCodeUrl());
        response.setValidUntil(qrCode.getValidUntil());
        response.setIsUsed(qrCode.getIsUsed());
        response.setUsedAt(qrCode.getUsedAt());
        response.setCreatedAt(qrCode.getCreatedAt());
        response.setType("DISCOUNT_COUPON");
        response.setRelatedId(qrCode.getCouponId());
        return response;
    }

    public MovieTicketQRCode toMovieTicketQRCode(QRCodeResponse response) {
        MovieTicketQRCode qrCode = new MovieTicketQRCode();
        qrCode.setId(response.getId());
        qrCode.setQrCodeData(response.getQrCodeData());
        qrCode.setQrCodeUrl(response.getQrCodeUrl());
        qrCode.setValidUntil(response.getValidUntil());
        qrCode.setIsUsed(response.getIsUsed());
        qrCode.setUsedAt(response.getUsedAt());
        qrCode.setCreatedAt(response.getCreatedAt());
        qrCode.setTicketId(response.getRelatedId());
        return qrCode;
    }

    public DiscountCouponQRCode toDiscountCouponQRCode(QRCodeResponse response) {
        DiscountCouponQRCode qrCode = new DiscountCouponQRCode();
        qrCode.setId(response.getId());
        qrCode.setQrCodeData(response.getQrCodeData());
        qrCode.setQrCodeUrl(response.getQrCodeUrl());
        qrCode.setValidUntil(response.getValidUntil());
        qrCode.setIsUsed(response.getIsUsed());
        qrCode.setUsedAt(response.getUsedAt());
        qrCode.setCreatedAt(response.getCreatedAt());
        qrCode.setCouponId(response.getRelatedId());
        return qrCode;
    }
}
