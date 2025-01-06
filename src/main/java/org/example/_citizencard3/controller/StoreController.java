    package org.example._citizencard3.controller;

    import lombok.RequiredArgsConstructor;
    import org.example._citizencard3.model.Store;
    import org.example._citizencard3.service.StoreService;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/stores")
    @RequiredArgsConstructor
    public class StoreController {

        private final StoreService storeService;

        // 創建商店
        @PostMapping
        public ResponseEntity<Store> createStore(@RequestBody Store store) {
            return ResponseEntity.ok(storeService.createStore(store));
        }

        // 取得單一商店
        @GetMapping("/{id}")
        public ResponseEntity<Store> getStore(@PathVariable Long id) {
            return ResponseEntity.ok(storeService.getStoreById(id));
        }

        // 取得所有商店（分頁）
        @GetMapping
        public ResponseEntity<Page<Store>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isDonation,
            @RequestParam(required = false) Boolean sortByPopularity
        ) {
            Pageable pageable = PageRequest.of(page, size);

            Page<Store> stores;
            if (area != null) {
                stores = storeService.getStoresByArea(area, pageable);
            } else if (category != null) {
                stores = storeService.getStoresByCategory(category, pageable);
            } else if (isDonation != null && isDonation) {
                stores = storeService.getDonationStores(pageable);
            } else if (sortByPopularity != null && sortByPopularity) {
                stores = storeService.getStoresByPopularity(pageable);
            } else {
                stores = storeService.getAllStores(pageable);
            }

            return ResponseEntity.ok(stores);
        }

        // 更新商店
        @PutMapping("/{id}")
        public ResponseEntity<Store> updateStore(
            @PathVariable Long id,
            @RequestBody Store storeDetails
        ) {
            return ResponseEntity.ok(storeService.updateStore(id, storeDetails));
        }

        // 刪除商店
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
            storeService.deleteStore(id);
            return ResponseEntity.ok().build();
        }

        // 根據區域查詢商店
        @GetMapping("/area/{area}")
        public ResponseEntity<Page<Store>> getStoresByArea(
            @PathVariable String area,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByArea(area, pageable));
        }

        // 根據分類查詢商店
        @GetMapping("/category/{category}")
        public ResponseEntity<Page<Store>> getStoresByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByCategory(category, pageable));
        }

        // 取得捐贈商店
        @GetMapping("/donation")
        public ResponseEntity<Page<Store>> getDonationStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getDonationStores(pageable));
        }
        // 根據優先度排序取得商店
        @GetMapping("/priority")
        public ResponseEntity<Page<Store>> getStoresByPriority(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByPriority(pageable));
        }

        // 取得高於特定優先度的商店
        @GetMapping("/priority/{threshold}")
        public ResponseEntity<Page<Store>> getStoresByPriorityThreshold(
            @PathVariable int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByPriorityThreshold(threshold, pageable));
        }

        // 根據人氣度排序取得商店
        @GetMapping("/popular")
        public ResponseEntity<Page<Store>> getStoresByPopular(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByPopularity(pageable));
        }

        // 根據人氣度取得商店
        @GetMapping("/popular/{threshold}")
        public ResponseEntity<Page<Store>> getStoresByPopularityThreshold(
            @PathVariable int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(storeService.getStoresByPopularityThreshold(threshold, pageable));
        }
    }