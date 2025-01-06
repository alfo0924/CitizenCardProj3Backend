package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.request.MovieRequest;
import org.example._citizencard3.dto.response.MovieResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.Movie;
import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.repository.MovieRepository;
import org.example._citizencard3.repository.ScheduleRepository;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;
    private final MovieTicketRepository movieTicketRepository;

    public Page<MovieResponse> getAllMovies(String title, String genre, int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<Movie> movies;

            if (title != null && !title.isEmpty()) {
                movies = movieRepository.findByTitleContaining(title, pageable);
            } else if (genre != null && !genre.isEmpty()) {
                movies = movieRepository.findByGenre(genre, pageable);
            } else {
                movies = movieRepository.findAll(pageable);
            }
            return movies.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting all movies:", e);
            throw new CustomException("獲取電影列表失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = findMovieById(id);
        return convertToResponse(movie);
    }

    public Page<MovieResponse> getNowShowingMovies(int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            LocalDateTime currentDate = LocalDateTime.now();
            return movieRepository.findNowShowingMovies(currentDate, pageable)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting now showing movies:", e);
            throw new CustomException("獲取上映中電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Page<MovieResponse> getComingSoonMovies(int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            LocalDateTime now = LocalDateTime.now();
            return movieRepository.findComingSoonMovies(now, pageable)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting coming soon movies:", e);
            throw new CustomException("獲取即將上映電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Page<MovieResponse> getMovieSchedules(Long movieId, int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            Movie movie = findMovieById(movieId);
            return movieRepository.findMovieSchedulesById(movieId, pageable)
                    .map(this::convertToResponse);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting movie schedules:", e);
            throw new CustomException("獲取電影場次失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Schedule> getActiveSchedules(Long movieId) {
        try {
            return scheduleRepository.findByMovieIdAndActive(movieId, true);
        } catch (Exception e) {
            log.error("Error getting active schedules:", e);
            throw new CustomException("獲取有效場次失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Page<MovieResponse> searchMovies(String keyword, int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            return movieRepository.searchMovies(keyword, pageable)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error searching movies:", e);
            throw new CustomException("搜尋電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Page<MovieResponse> getMoviesByGenre(String genre, int page, int size, String sort) {
        try {
            Sort sorting = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sorting);
            return movieRepository.findByGenre(genre, pageable)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting movies by genre:", e);
            throw new CustomException("獲取電影類型失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MovieResponse toggleMovieStatus(Long id) {
        try {
            Movie movie = findMovieById(id);
            movie.setIsShowing(!movie.getIsShowing());
            movie.setUpdatedAt(LocalDateTime.now());
            return convertToResponse(movieRepository.save(movie));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error toggling movie status:", e);
            throw new CustomException("切換電影狀態失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        try {
            validateMovieRequest(request);
            LocalDateTime now = LocalDateTime.now();

            Movie movie = Movie.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .director(request.getDirector())
                    .cast(request.getCast())
                    .duration(request.getDuration())
                    .genre(request.getGenre())
                    .rating(request.getRating())
                    .posterUrl(request.getPosterUrl())
                    .trailerUrl(request.getTrailerUrl())
                    .releaseDate(request.getReleaseDate())
                    .endDate(request.getEndDate())
                    .isShowing(request.getIsShowing())
                    .price(request.getPrice())
                    .score(0.0)
                    .active(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            return convertToResponse(movieRepository.save(movie));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating movie:", e);
            throw new CustomException("創建電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        try {
            Movie movie = findMovieById(id);

            // 只在請求中包含值時才更新
            if (request.getTitle() != null) {
                movie.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                movie.setDescription(request.getDescription());
            }
            if (request.getDirector() != null) {
                movie.setDirector(request.getDirector());
            }
            if (request.getCast() != null) {
                movie.setCast(request.getCast());
            }
            if (request.getDuration() != null) {
                movie.setDuration(request.getDuration());
            }
            if (request.getGenre() != null) {
                movie.setGenre(request.getGenre());
            }
            if (request.getRating() != null) {
                movie.setRating(request.getRating());
            }
            if (request.getPosterUrl() != null) {
                movie.setPosterUrl(request.getPosterUrl());
            }
            if (request.getTrailerUrl() != null) {
                movie.setTrailerUrl(request.getTrailerUrl());
            }
            if (request.getReleaseDate() != null) {
                movie.setReleaseDate(request.getReleaseDate());
            }
            if (request.getEndDate() != null) {
                movie.setEndDate(request.getEndDate());
            }
            if (request.getIsShowing() != null) {
                movie.setIsShowing(request.getIsShowing());
            }
            if (request.getPrice() != null) {
                movie.setPrice(request.getPrice());
            }

            movie.setUpdatedAt(LocalDateTime.now());

            return convertToResponse(movieRepository.save(movie));
        } catch (Exception e) {
            log.error("Error updating movie:", e);
            throw new CustomException("更新電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void deleteMovie(Long id) {
        try {
            Movie movie = findMovieById(id);

            List<Schedule> activeSchedules = scheduleRepository.findByMovieIdAndActive(id, true);
            if (!activeSchedules.isEmpty()) {
                throw new CustomException("無法刪除有效場次的電影", HttpStatus.BAD_REQUEST);
            }

            movie.setActive(false);
            movie.setIsShowing(false);
            movie.setUpdatedAt(LocalDateTime.now());
            movieRepository.save(movie);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting movie:", e);
            throw new CustomException("刪除電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Map<String, Object>> getRecentBookings(int limit) {
        try {
            return movieTicketRepository.findRecentTicketsByUser(null, PageRequest.of(0, limit))
                    .stream()
                    .map(ticket -> {
                        Map<String, Object> booking = new HashMap<>();
                        booking.put("id", ticket.getId());
                        booking.put("movieTitle", ticket.getMovie().getTitle());
                        booking.put("showTime", ticket.getSchedule().getShowTime());
                        booking.put("seatNumber", ticket.getSeatNumber());
                        booking.put("status", ticket.getStatus());
                        booking.put("createdAt", ticket.getCreatedAt());
                        return booking;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent bookings:", e);
            throw new CustomException("獲取最近訂票記錄失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long countActiveMovies() {
        try {
            return movieRepository.countByIsShowingTrueAndActiveTrue();
        } catch (Exception e) {
            log.error("Error counting active movies:", e);
            throw new CustomException("統計上映中電影數量失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long countNewMoviesAfter(LocalDateTime date) {
        try {
            return movieRepository.countByCreatedAtAfterAndActiveTrue(date);
        } catch (Exception e) {
            log.error("Error counting new movies:", e);
            throw new CustomException("統計新電影數量失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Movie findMovieById(Long id) {
        try {
            return movieRepository.findById(id)
                    .orElseThrow(() -> new CustomException(
                            "找不到指定電影",
                            HttpStatus.NOT_FOUND
                    ));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding movie by id:", e);
            throw new CustomException("查詢電影失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateMovieRequest(MovieRequest request) {
        // 只在更新日期時進行驗證
        if (request.getReleaseDate() != null && request.getEndDate() != null) {
            if (request.getReleaseDate().isAfter(request.getEndDate())) {
                throw new CustomException("上映日期不能晚於下檔日期", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getPrice() != null && request.getPrice() < 0) {
            throw new CustomException("票價不能小於0", HttpStatus.BAD_REQUEST);
        }

        if (request.getDuration() != null && request.getDuration() <= 0) {
            throw new CustomException("片長必須大於0", HttpStatus.BAD_REQUEST);
        }
    }



    private MovieResponse convertToResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .duration(movie.getDuration())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .releaseDate(movie.getReleaseDate())
                .endDate(movie.getEndDate())
                .isShowing(movie.getIsShowing())
                .price(movie.getPrice())
                .score(movie.getScore())
                .active(movie.getActive())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }

    private Sort createSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}