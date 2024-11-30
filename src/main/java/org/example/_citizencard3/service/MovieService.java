package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.MovieRequest;
import org.example._citizencard3.dto.response.MovieResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.Movie;
import org.example._citizencard3.repositroy.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    // 獲取所有電影
    public List<MovieResponse> getAllMovies(String title, String genre, int page, int size) {
        Page<Movie> movies;
        if (title != null && !title.isEmpty()) {
            movies = movieRepository.findByTitleContaining(title, Pageable.ofSize(size).withPage(page));
        } else if (genre != null && !genre.isEmpty()) {
            movies = movieRepository.findByGenre(genre, Pageable.ofSize(size).withPage(page));
        } else {
            movies = movieRepository.findAll(Pageable.ofSize(size).withPage(page));
        }
        return movies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 獲取單部電影
    public MovieResponse getMovieById(Long id) {
        Movie movie = findMovieById(id);
        return convertToResponse(movie);
    }

    // 新增電影
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        validateMovieRequest(request);
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
                .active(true)
                .build();

        movie = movieRepository.save(movie);
        return convertToResponse(movie);
    }

    // 更新電影
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = findMovieById(id);
        validateMovieRequest(request);

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setDuration(request.getDuration());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());
        movie.setIsShowing(request.getIsShowing());
        movie.setPrice(request.getPrice());

        movie = movieRepository.save(movie);
        return convertToResponse(movie);
    }

    // 刪除電影
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findMovieById(id);
        movie.setActive(false);
        movieRepository.save(movie);
    }

    // 獲取正在上映的電影
    public List<MovieResponse> getNowShowingMovies() {
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.findByIsShowingTrueAndReleaseDateBeforeAndEndDateAfter(now, now)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 獲取即將上映的電影
    public List<MovieResponse> getComingSoonMovies() {
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.findByReleaseDateAfter(now)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 獲取電影場次
    public List<MovieResponse> getMovieSchedules(Long movieId) {
        Movie movie = findMovieById(movieId);
        return movie.getSchedules().stream()
                .map(schedule -> MovieResponse.builder()
                        .id(schedule.getId())
                        .showTime(schedule.getShowTime())
                        .hall(schedule.getHall())
                        .availableSeats(schedule.getAvailableSeats())
                        .build())
                .collect(Collectors.toList());
    }

    // 輔助方法：查找電影
    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "找不到指定電影",
                        HttpStatus.NOT_FOUND
                ));
    }

    // 輔助方法：驗證請求
    private void validateMovieRequest(MovieRequest request) {
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new CustomException(
                    "上映日期不能晚於下檔日期",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // 輔助方法：轉換為響應對象
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
                .build();
    }
}