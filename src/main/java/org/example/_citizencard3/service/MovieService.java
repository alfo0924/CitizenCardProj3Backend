package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.MovieRequest;
import org.example._citizencard3.dto.response.MovieResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.Movie;
import org.example._citizencard3.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Page<MovieResponse> getAllMovies(String title, String genre, int page, int size, String sort) {
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
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = findMovieById(id);
        return convertToResponse(movie);
    }

    public Page<MovieResponse> getNowShowingMovies(int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        LocalDateTime currentDate = LocalDateTime.now();
        return movieRepository.findNowShowingMovies(currentDate, pageable)
                .map(this::convertToResponse);
    }

    public Page<MovieResponse> getComingSoonMovies(int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.findComingSoonMovies(now, pageable)
                .map(this::convertToResponse);
    }

    public Page<MovieResponse> getMovieSchedules(Long movieId, int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Movie movie = findMovieById(movieId);
        return movieRepository.findMovieSchedulesById(movieId, pageable)
                .map(this::convertToResponse);
    }

    public Page<MovieResponse> searchMovies(String keyword, int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        return movieRepository.searchMovies(keyword, pageable)
                .map(this::convertToResponse);
    }

    public Page<MovieResponse> getMoviesByGenre(String genre, int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        return movieRepository.findByGenre(genre, pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public MovieResponse toggleMovieStatus(Long id) {
        Movie movie = findMovieById(id);
        movie.setIsShowing(!movie.getIsShowing());
        movie.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(movieRepository.save(movie));
    }

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
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
    }

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
        movie.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findMovieById(id);
        movie.setActive(false);
        movie.setUpdatedAt(LocalDateTime.now());
        movieRepository.save(movie);
    }

    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "找不到指定電影",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateMovieRequest(MovieRequest request) {
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new CustomException(
                    "上映日期不能晚於下檔日期",
                    HttpStatus.BAD_REQUEST
            );
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
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    public long countActiveMovies() {
        return movieRepository.countByIsShowingTrueAndActiveTrue();
    }

    public long countNewMoviesAfter(LocalDateTime oneMonthAgo) {
        return movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo);
    }

    public Object getRecentBookings(int i) {
        return null;
    }
}