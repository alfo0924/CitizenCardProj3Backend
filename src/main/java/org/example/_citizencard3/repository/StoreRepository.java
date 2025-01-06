package org.example._citizencard3.repository;

import org.example._citizencard3.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Page<Store> findByArea(String area, Pageable pageable);
    Page<Store> findByCategory(String category, Pageable pageable);
    Page<Store> findByIsDonationTrue(Pageable pageable);
    Page<Store> findAllByOrderByPriorityDesc(Pageable pageable);
    Page<Store> findAllByOrderByPopularityDesc(Pageable pageable);
    Page<Store> findByPriorityGreaterThanEqual(int priority, Pageable pageable);
    Page<Store> findByPopularityGreaterThanEqual(int popularity, Pageable pageable);
}