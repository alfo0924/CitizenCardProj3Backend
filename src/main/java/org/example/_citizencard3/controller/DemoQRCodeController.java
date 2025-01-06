package org.example._citizencard3.controller;

import java.util.Map;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.MovieTicketQRCode;
import org.example._citizencard3.service.QRCodeService;
import org.example._citizencard3.service.MovieTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController  // 改用@RestController，不是@Controller
@RequestMapping("/demo/qrcode")
public class DemoQRCodeController {

  @Autowired
  private QRCodeService qrCodeService;

  @Autowired
  private MovieTicketService movieTicketService;

  @GetMapping("/show/{ticketId}")
  public ResponseEntity<?> showQRCode(@PathVariable Long ticketId) {
    try {
      MovieTicketQRCode qrCode;
      try {
        qrCode = qrCodeService.getMovieTicketQRCode(ticketId);
      } catch (RuntimeException e) {
        MovieTicket ticket = movieTicketService.getTicketById(ticketId);
        qrCode = qrCodeService.generateMovieTicketQRCode(ticketId, ticket.getSchedule().getShowTime());
      }

      return ResponseEntity.ok(Map.of(
          "ticketId", ticketId,
          "qrCodeData", qrCode.getQrCodeData(),
          "qrCodeImage", "data:image/png;base64," + qrCodeService.generateQRCodeImage(qrCode.getQrCodeData())
      ));
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of(
          "error", "QR Code處理失敗：" + e.getMessage()
      ));
    }
  }

  // REST API端點
  @RestController
  @RequestMapping("/demo/qrcode")
  public static class DemoQRCodeRestController {
    @Autowired
    private QRCodeService qrCodeService;

    // 驗證並使用票券
    @GetMapping("/verify-ticket")
    public ResponseEntity<?> verifyAndUseTicket(@RequestParam String qrCodeData) {
      try {
        Map<String, Object> ticketInfo = qrCodeService.getTicketVerificationInfo(qrCodeData);
        Long ticketId = Long.parseLong(ticketInfo.get("ticketId").toString());

        boolean isValid = qrCodeService.validateMovieTicketQRCode(qrCodeData, ticketId);

        if (isValid) {
          qrCodeService.markMovieTicketQRCodeAsUsed(qrCodeData);
          return ResponseEntity.ok().body(Map.of(
              "success", true,
              "message", "票券驗證成功",
              "ticketInfo", ticketInfo
          ));
        } else {
          return ResponseEntity.ok().body(Map.of(
              "success", false,
              "message", "票券無效或已使用"
          ));
        }
      } catch (Exception e) {
        return ResponseEntity.ok().body(Map.of(
            "success", false,
            "message", "驗證失敗：" + e.getMessage()
        ));
      }
    }

    // 查看票券資訊
    @GetMapping("/ticket-info")
    public ResponseEntity<?> getTicketInfo(@RequestParam String qrCodeData) {
      try {
        Map<String, Object> ticketInfo = qrCodeService.getTicketVerificationInfo(qrCodeData);
        return ResponseEntity.ok().body(Map.of(
            "success", true,
            "ticketInfo", ticketInfo
        ));
      } catch (Exception e) {
        return ResponseEntity.ok().body(Map.of(
            "success", false,
            "message", "解析失敗：" + e.getMessage()
        ));
      }
    }
  }
}