package org.example._citizencard3.mapper;

import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.model.MovieTicket;
import org.springframework.stereotype.Component;

@Component
public class MovieTicketMapper {

    public MovieTicketResponse toResponse(MovieTicket ticket) {
        if (ticket == null) {
            return null;
        }

        return MovieTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUser().getId())
                .movieTitle(ticket.getMovie().getTitle())
                .hall(ticket.getSchedule().getHall())
                .seatNumber(ticket.getSeatNumber())
                .status(String.valueOf(ticket.getStatus()))
                .showTime(ticket.getSchedule().getShowTime())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                // 額外的電影資訊
                .moviePoster(ticket.getMovie().getPosterUrl())
                .movieDirector(ticket.getMovie().getDirector())
                .movieCast(ticket.getMovie().getCast())
                .movieDuration(ticket.getMovie().getDuration())
                .movieGenre(ticket.getMovie().getGenre())
                .movieRating(ticket.getMovie().getRating())
                // 場次資訊
                .scheduleHall(ticket.getSchedule().getHall())
                .totalSeats(ticket.getSchedule().getTotalSeats())
                .availableSeats(ticket.getSchedule().getAvailableSeats())
                // 票券狀態
                .isValid("VALID".equals(ticket.getStatus()))
                .isUsed("USED".equals(ticket.getStatus()))
                .isExpired("EXPIRED".equals(ticket.getStatus()))
                .isCancelled("CANCELLED".equals(ticket.getStatus()))
                .build();
    }
}