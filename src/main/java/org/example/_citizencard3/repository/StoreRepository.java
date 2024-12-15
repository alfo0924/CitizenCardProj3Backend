package org.example._citizencard3.repository;

import org.example._citizencard3.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // 基本查詢方法
    Optional<Store> findByIdAndActiveTrue(Long id);

    Page<Store> findByActiveTrue(Pageable pageable);

    // 搜尋相關
    Page<Store> findByNameContainingAndActiveTrue(String name, Pageable pageable);

    Page<Store> findByNameContainingAndCategoryAndActiveTrue(String name, String category, Pageable pageable);

    Page<Store> findByCategoryAndActiveTrue(String category, Pageable pageable);

    List<Store> findByCategoryAndActiveTrue(String category);
    List<Store> findByOrderByRatingCountDesc();

    // 自定義查詢
    @Query("SELECT DISTINCT s.category FROM Store s WHERE s.active = true")
    List<String> findAllCategories();

    @Query("SELECT s FROM Store s WHERE s.active = true AND " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Store> searchStores(@Param("query") String query, Pageable pageable);

    // 推薦商店查詢
    @Query("SELECT s FROM Store s WHERE s.active = true " +
            "ORDER BY s.rating DESC, s.ratingCount DESC")
    List<Store> findRecommendedStores();

    // 統計相關查詢
    @Query("SELECT COUNT(s) FROM Store s WHERE s.active = true")
    long countActiveStores();

    @Query("SELECT AVG(s.rating) FROM Store s WHERE s.active = true")
    Double getAverageRating();

    // 區域查詢
    @Query("SELECT s FROM Store s WHERE s.active = true " +
            "AND s.latitude BETWEEN :minLat AND :maxLat " +
            "AND s.longitude BETWEEN :minLng AND :maxLng")
    List<Store> findStoresInArea(@Param("minLat") Double minLat,
                                 @Param("maxLat") Double maxLat,
                                 @Param("minLng") Double minLng,
                                 @Param("maxLng") Double maxLng);

    // 合作狀態查詢
    List<Store> findByPartnershipStatusAndActiveTrue(Store.PartnershipStatus status);

    /**
     * 計算所有活躍的商店數量
     * @return 活躍商店的數量
     */
    long countByActiveTrue();

    /**
     * 計算在指定日期之後創建的活躍商店數量
     * @param oneMonthAgo 指定的日期
     * @return 在指定日期之後創建的活躍商店數量
     */
    long countByActiveTrueAndCreatedAtAfter(LocalDateTime oneMonthAgo);

    /**
     * 計算每個類別的活躍商店數量
     * @return 包含每個類別及其對應的活躍商店數量的Map
     */
    @Query("SELECT s.category, COUNT(s) FROM Store s WHERE s.active = true GROUP BY s.category")
    Map<String, Long> countByCategory();
}