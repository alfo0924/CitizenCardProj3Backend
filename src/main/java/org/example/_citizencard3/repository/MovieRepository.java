package org.example._citizencard3.repository;

import org.example._citizencard3.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContaining(String title);

    List<Movie> findByGenre(String genre);

    @Query("SELECT m FROM Movie m WHERE m.isShowing = true AND m.releaseDate <= :now AND m.endDate >= :now")
    List<Movie> findNowShowingMovies(@Param("now") LocalDateTime now);

    List<Movie> findByReleaseDateAfter(LocalDateTime date);

    @Query("SELECT m FROM Movie m WHERE m.isShowing = true ORDER BY m.score DESC")
    List<Movie> findTopRatedMovies();

    @Query("SELECT DISTINCT m.genre FROM Movie m")
    List<String> findAllGenres();

    @Query("SELECT m FROM Movie m WHERE " +
            "LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.director) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.cast) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Movie> searchMovies(@Param("query") String query);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate BETWEEN :startDate AND :endDate")
    List<Movie> findMoviesByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(m) FROM Movie m WHERE m.isShowing = true")
    long countActiveMovies();

    @Query("SELECT AVG(m.price) FROM Movie m WHERE m.isShowing = true")
    Double getAverageTicketPrice();
    Page<Movie> findByTitleContaining(String title, Pageable pageable);

    Page<Movie> findByGenre(String genre, Pageable pageable);

    List<Movie> findByIsShowingTrueAndReleaseDateBeforeAndEndDateAfter(
            LocalDateTime releaseDate,
            LocalDateTime endDate
    );

}