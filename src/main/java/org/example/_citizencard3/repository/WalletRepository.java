package org.example._citizencard3.repository;

import org.example._citizencard3.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // 根據餘額範圍查詢
    List<Wallet> findByBalanceBetween(Double minBalance, Double maxBalance);

    // 根據更新時間查詢
    List<Wallet> findByUpdatedAtAfter(LocalDateTime date);

    // 查詢餘額大於指定金額的錢包
    List<Wallet> findByBalanceGreaterThan(Double amount);

    // 查詢餘額小於指定金額的錢包
    List<Wallet> findByBalanceLessThan(Double amount);
}