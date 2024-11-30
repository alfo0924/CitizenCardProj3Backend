package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.StoreRequest;
import org.example._citizencard3.dto.response.StoreResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.Store;

import org.example._citizencard3.repositroy.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    // 獲取所有商店
    public Page<StoreResponse> getAllStores(String keyword, String category, Pageable pageable) {
        Page<Store> stores;
        if (keyword != null && category != null) {
            stores = storeRepository.findByNameContainingAndCategoryAndActiveTrue(keyword, category, pageable);
        } else if (keyword != null) {
            stores = storeRepository.findByNameContainingAndActiveTrue(keyword, pageable);
        } else if (category != null) {
            stores = storeRepository.findByCategoryAndActiveTrue(category, pageable);
        } else {
            stores = storeRepository.findByActiveTrue(pageable);
        }
        return stores.map(this::convertToResponse);
    }

    // 獲取單個商店
    public StoreResponse getStoreById(Long id) {
        Store store = storeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new CustomException("商店不存在", HttpStatus.NOT_FOUND));
        return convertToResponse(store);
    }

    // 創建商店
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        validateStoreRequest(request);
        Store store = Store.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .openingHours(request.getOpeningHours())
                .imageUrl(request.getImageUrl())
                .discountInfo(request.getDiscountInfo())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .active(true)
                .build();

        store = storeRepository.save(store);
        return convertToResponse(store);
    }

    // 更新商店
    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        Store store = storeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new CustomException("商店不存在", HttpStatus.NOT_FOUND));

        validateStoreRequest(request);

        store.setName(request.getName());
        store.setCategory(request.getCategory());
        store.setDescription(request.getDescription());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setWebsite(request.getWebsite());
        store.setOpeningHours(request.getOpeningHours());
        store.setImageUrl(request.getImageUrl());
        store.setDiscountInfo(request.getDiscountInfo());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());

        store = storeRepository.save(store);
        return convertToResponse(store);
    }

    // 刪除商店
    @Transactional
    public void deleteStore(Long id) {
        Store store = storeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new CustomException("商店不存在", HttpStatus.NOT_FOUND));
        store.softDelete();
        storeRepository.save(store);
    }

    // 獲取所有分類
    public List<String> getAllCategories() {
        return storeRepository.findAllCategories();
    }

    // 根據分類獲取商店
    public List<StoreResponse> getStoresByCategory(String category) {
        List<Store> stores = storeRepository.findByCategoryAndActiveTrue(category);
        return stores.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 搜索商店
    public Page<StoreResponse> searchStores(String query, Pageable pageable) {
        Page<Store> stores = storeRepository.searchStores(query, pageable);
        return stores.map(this::convertToResponse);
    }

    // 獲取推薦商店
    public List<StoreResponse> getRecommendedStores() {
        List<Store> stores = storeRepository.findRecommendedStores();
        return stores.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 驗證請求
    private void validateStoreRequest(StoreRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new CustomException("商店名稱不能為空", HttpStatus.BAD_REQUEST);
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new CustomException("商店類別不能為空", HttpStatus.BAD_REQUEST);
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new CustomException("商店地址不能為空", HttpStatus.BAD_REQUEST);
        }
    }

    // 轉換為響應對象
    private StoreResponse convertToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .category(store.getCategory())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhone())
                .email(store.getEmail())
                .website(store.getWebsite())
                .openingHours(store.getOpeningHours())
                .imageUrl(store.getImageUrl())
                .discountInfo(store.getDiscountInfo())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .rating(store.getRating())
                .ratingCount(store.getRatingCount())
                .build();
    }
    public List<StoreResponse> getPopularStores() {
        // 根據評分數量降序排序，取前10個商店
        List<Store> popularStores = storeRepository.findByOrderByRatingCountDesc()
                .stream()
                .limit(10)
                .toList();

        return popularStores.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}