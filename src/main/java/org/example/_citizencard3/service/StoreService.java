package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.model.Store;
import org.example._citizencard3.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    // 創建商店
    @Transactional
    public Store createStore(Store store) {
        // 可以加入驗證邏輯
        validateStore(store);
        return storeRepository.save(store);
    }

    // 根據 ID 查詢商店
    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

    // 分頁查詢所有商店
    public Page<Store> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable);
    }

    // 更新商店
    @Transactional
    public Store updateStore(Long id, Store storeDetails) {
        Store store = getStoreById(id);

        // 更新欄位
        store.setName(storeDetails.getName());
        store.setArea(storeDetails.getArea());
        store.setTag(storeDetails.getTag());
        store.setContent(storeDetails.getContent());
        store.setWebsite(storeDetails.getWebsite());
        store.setCategory(storeDetails.getCategory());
        store.setShortContent(storeDetails.getShortContent());
        store.setTime(storeDetails.getTime());
        store.setAddress(storeDetails.getAddress());
        store.setPhone(storeDetails.getPhone());
        store.setPriority(storeDetails.getPriority());
        store.setPopularity(storeDetails.getPopularity());
        store.setIsDonation(storeDetails.getIsDonation());
        store.setIframeSrc(storeDetails.getIframeSrc());
        store.setImgUrl(storeDetails.getImgUrl());

        return storeRepository.save(store);
    }

    // 刪除商店
    @Transactional
    public void deleteStore(Long id) {
        Store store = getStoreById(id);
        storeRepository.delete(store);
    }

    // 根據區域查詢商店
    public Page<Store> getStoresByArea(String area, Pageable pageable) {
        return storeRepository.findByArea(area, pageable);
    }

    // 根據分類查詢商店
    public Page<Store> getStoresByCategory(String category, Pageable pageable) {
        return storeRepository.findByCategory(category, pageable);
    }

    // 查詢捐贈商店
    public Page<Store> getDonationStores(Pageable pageable) {
        return storeRepository.findByIsDonationTrue(pageable);
    }

    // 根據優先度排序查詢商店
    public Page<Store> getStoresByPriority(Pageable pageable) {
        return storeRepository.findAllByOrderByPriorityDesc(pageable);
    }

    // 查詢高於特定優先度的商店
    public Page<Store> getStoresByPriorityThreshold(int priority, Pageable pageable) {
        return storeRepository.findByPriorityGreaterThanEqual(priority, pageable);
    }

    // 根據熱門度排序查詢商店
    public Page<Store> getStoresByPopularity(Pageable pageable) {
        return storeRepository.findAllByOrderByPopularityDesc(pageable);
    }

    // 查詢高於特定人氣度的商店
    public Page<Store> getStoresByPopularityThreshold(int popularity, Pageable pageable) {
        return storeRepository.findByPopularityGreaterThanEqual(popularity, pageable);
    }

    // 驗證商店資料
    private void validateStore(Store store) {
        if (store.getName() == null || store.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }
        if (store.getArea() == null || store.getArea().trim().isEmpty()) {
            throw new IllegalArgumentException("Store area cannot be empty");
        }
        if (store.getTag() == null || store.getTag().trim().isEmpty()) {
            throw new IllegalArgumentException("Store tag cannot be empty");
        }
        if (store.getContent() == null || store.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Store content cannot be empty");
        }
        // 可以加入更多驗證邏輯
    }
}