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
@RequestMapping("/stores")  // 移除/api前綴，因為已經在application.properties中設置了
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<Page<StoreResponse>> getAllStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return ResponseEntity.ok(storeService.getAllStores(keyword, category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(storeService.getAllCategories());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<StoreResponse>> getStoresByCategory(@PathVariable String category) {
        return ResponseEntity.ok(storeService.getStoresByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StoreResponse>> searchStores(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(storeService.searchStores(query, pageable));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<StoreResponse>> getRecommendedStores() {
        return ResponseEntity.ok(storeService.getRecommendedStores());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<StoreResponse>> getPopularStores() {
        return ResponseEntity.ok(storeService.getPopularStores());
    }
}