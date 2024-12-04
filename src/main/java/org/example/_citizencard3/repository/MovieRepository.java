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

    // 基本查詢方法
    Page<Movie> findByTitleContaining(String title, Pageable pageable);

    Page<Movie> findByGenre(String genre, Pageable pageable);

    // 正在上映的電影
    @Query("SELECT m FROM Movie m WHERE m.isShowing = true " +
            "AND m.releaseDate <= :now " +
            "AND m.endDate >= :now " +
            "AND m.active = true")
    Page<Movie> findNowShowingMovies(@Param("now") LocalDateTime now, Pageable pageable);

    // 即將上映的電影
    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :now AND m.active = true")
    Page<Movie> findComingSoonMovies(@Param("now") LocalDateTime now, Pageable pageable);

    // 電影場次查詢
    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.schedules s WHERE m.id = :movieId AND m.active = true")
    Page<Movie> findMovieSchedulesById(@Param("movieId") Long movieId, Pageable pageable);

    // 標題或描述模糊搜索
    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
            "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Movie> searchMovies(@Param("keyword") String keyword, Pageable pageable);

    // 熱門電影查詢
    @Query("SELECT m FROM Movie m WHERE m.isShowing = true AND m.active = true ORDER BY m.score DESC")
    List<Movie> findTopRatedMovies();

    // 獲取所有電影類型
    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.active = true")
    List<String> findAllGenres();

    // 綜合搜索
    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
            "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.director) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.cast) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Movie> searchByKeyword(@Param("query") String query);

    // 日期範圍查詢
    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
            "m.releaseDate BETWEEN :startDate AND :endDate")
    List<Movie> findMoviesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 統計查詢
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.isShowing = true AND m.active = true")
    long countActiveMovies();

    @Query("SELECT AVG(m.price) FROM Movie m WHERE m.isShowing = true AND m.active = true")
    Double getAverageTicketPrice();
}