package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.MovieRequest;
import org.example._citizencard3.dto.response.MovieResponse;
import org.example._citizencard3.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class MovieController {

    private final MovieService movieService;

    // 獲取所有電影列表
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<MovieResponse> movies = movieService.getAllMovies(title, genre, page, size);
        return ResponseEntity.ok(movies);
    }

    // 獲取單部電影詳情
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    // 新增電影 (僅管理員)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.ok(movie);
    }

    // 更新電影信息 (僅管理員)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request
    ) {
        MovieResponse movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(movie);
    }

    // 刪除電影 (僅管理員)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }

    // 獲取正在上映的電影
    @GetMapping("/now-showing")
    public ResponseEntity<List<MovieResponse>> getNowShowingMovies() {
        List<MovieResponse> movies = movieService.getNowShowingMovies();
        return ResponseEntity.ok(movies);
    }

    // 獲取即將上映的電影
    @GetMapping("/coming-soon")
    public ResponseEntity<List<MovieResponse>> getComingSoonMovies() {
        List<MovieResponse> movies = movieService.getComingSoonMovies();
        return ResponseEntity.ok(movies);
    }

    // 獲取電影場次
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<MovieResponse>> getMovieSchedules(@PathVariable Long id) {
        List<MovieResponse> schedules = movieService.getMovieSchedules(id);
        return ResponseEntity.ok(schedules);
    }

    // 處理錯誤
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}