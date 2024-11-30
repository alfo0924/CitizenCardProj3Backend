package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.StoreRequest;
import org.example._citizencard3.dto.response.StoreResponse;
import org.example._citizencard3.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class StoreController {

    private final StoreService storeService;

    // 獲取所有商店列表
    @GetMapping
    public ResponseEntity<Page<StoreResponse>> getAllStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<StoreResponse> stores = storeService.getAllStores(keyword, category, pageable);
        return ResponseEntity.ok(stores);
    }

    // 獲取單個商店詳情
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long id) {
        StoreResponse store = storeService.getStoreById(id);
        return ResponseEntity.ok(store);
    }

    // 新增商店 (僅管理員)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity.ok(store);
    }

    // 更新商店資訊 (僅管理員)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.updateStore(id, request);
        return ResponseEntity.ok(store);
    }

    // 刪除商店 (僅管理員)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok().build();
    }

    // 獲取商店分類列表
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = storeService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // 根據分類獲取商店
    @GetMapping("/category/{category}")
    public ResponseEntity<List<StoreResponse>> getStoresByCategory(@PathVariable String category) {
        List<StoreResponse> stores = storeService.getStoresByCategory(category);
        return ResponseEntity.ok(stores);
    }

    // 搜尋商店
    @GetMapping("/search")
    public ResponseEntity<Page<StoreResponse>> searchStores(
            @RequestParam String query,
            Pageable pageable) {
        Page<StoreResponse> stores = storeService.searchStores(query, pageable);
        return ResponseEntity.ok(stores);
    }

    // 獲取推薦商店
    @GetMapping("/recommended")
    public ResponseEntity<List<StoreResponse>> getRecommendedStores() {
        List<StoreResponse> stores = storeService.getRecommendedStores();
        return ResponseEntity.ok(stores);
    }

    // 獲取熱門商店
    @GetMapping("/popular")
    public ResponseEntity<List<StoreResponse>> getPopularStores() {
        List<StoreResponse> stores = storeService.getPopularStores();
        return ResponseEntity.ok(stores);
    }

    // 處理異常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}