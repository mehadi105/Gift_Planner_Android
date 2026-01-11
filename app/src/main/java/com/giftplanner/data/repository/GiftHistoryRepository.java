package com.giftplanner.data.repository;

import androidx.lifecycle.LiveData;
import com.giftplanner.data.dao.GiftHistoryDao;
import com.giftplanner.data.entity.GiftHistory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GiftHistoryRepository {
    private final GiftHistoryDao giftHistoryDao;
    private final ExecutorService executorService;
    
    public GiftHistoryRepository(GiftHistoryDao giftHistoryDao) {
        this.giftHistoryDao = giftHistoryDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<GiftHistory>> getAllGiftHistory(long userId) {
        return giftHistoryDao.getAllGiftHistoryByUser(userId);
    }
    
    public void insertGiftHistory(GiftHistory giftHistory, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = giftHistoryDao.insert(giftHistory);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateGiftHistory(GiftHistory giftHistory, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                giftHistoryDao.update(giftHistory);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteGiftHistory(GiftHistory giftHistory, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                giftHistoryDao.delete(giftHistory);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getGiftHistoryById(long id, RepositoryCallback<GiftHistory> callback) {
        executorService.execute(() -> {
            try {
                GiftHistory giftHistory = giftHistoryDao.getGiftHistoryById(id);
                callback.onSuccess(giftHistory);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getGiftHistoryByDateRange(long userId, String startDate, String endDate, 
                                         RepositoryCallback<List<GiftHistory>> callback) {
        executorService.execute(() -> {
            try {
                List<GiftHistory> history = giftHistoryDao.getGiftHistoryByDateRange(userId, startDate, endDate);
                callback.onSuccess(history);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}


