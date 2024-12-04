package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.MovieRequest;
import org.example._citizencard3.dto.response.MovieResponse;
import org.example._citizencard3.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate,desc") String sort
    ) {
        Page<MovieResponse> movies = movieService.getAllMovies(title, genre, page, size, sort);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request
    ) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/now-showing")
    public ResponseEntity<Page<MovieResponse>> getNowShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score,desc") String sort
    ) {
        return ResponseEntity.ok(movieService.getNowShowingMovies(page, size, sort));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<Page<MovieResponse>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate,asc") String sort
    ) {
        return ResponseEntity.ok(movieService.getComingSoonMovies(page, size, sort));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<Page<MovieResponse>> getMovieSchedules(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "showtime,asc") String sort
    ) {
        return ResponseEntity.ok(movieService.getMovieSchedules(id, page, size, sort));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate,desc") String sort
    ) {
        return ResponseEntity.ok(movieService.searchMovies(keyword, page, size, sort));
    }

    @GetMapping("/by-genre/{genre}")
    public ResponseEntity<Page<MovieResponse>> getMoviesByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate,desc") String sort
    ) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre, page, size, sort));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> toggleMovieStatus(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.toggleMovieStatus(id));
    }
}