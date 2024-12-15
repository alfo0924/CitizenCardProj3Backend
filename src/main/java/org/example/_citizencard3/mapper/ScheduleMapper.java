package org.example._citizencard3.mapper;

import org.example._citizencard3.dto.request.ScheduleRequest;
import org.example._citizencard3.dto.response.ScheduleResponse;
import org.example._citizencard3.model.Schedule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    public Schedule toEntity(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setMovieId(request.getMovieId());
        schedule.setShowTime(request.getShowTime());
        schedule.setHall(request.getHall());
        schedule.setTotalSeats(request.getTotalSeats());
        schedule.setAvailableSeats(request.getAvailableSeats());
        schedule.setActive(request.getActive());
        return schedule;
    }

    public ScheduleResponse toResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setMovieId(schedule.getMovieId());
        response.setShowTime(schedule.getShowTime());
        response.setHall(schedule.getHall());
        response.setTotalSeats(schedule.getTotalSeats());
        response.setAvailableSeats(schedule.getAvailableSeats());
        response.setActive(schedule.isActive());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
    }

    public List<ScheduleResponse> toResponseList(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateEntity(Schedule schedule, ScheduleRequest request) {
        schedule.setMovieId(request.getMovieId());
        schedule.setShowTime(request.getShowTime());
        schedule.setHall(request.getHall());
        schedule.setTotalSeats(request.getTotalSeats());
        schedule.setAvailableSeats(request.getAvailableSeats());
        schedule.setActive(request.getActive());
    }
}
