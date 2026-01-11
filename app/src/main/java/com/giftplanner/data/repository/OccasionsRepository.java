package com.giftplanner.data.repository;

import androidx.lifecycle.LiveData;
import com.giftplanner.data.dao.OccasionDao;
import com.giftplanner.data.entity.Occasion;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OccasionsRepository {
    private final OccasionDao occasionDao;
    private final ExecutorService executorService;
    
    public OccasionsRepository(OccasionDao occasionDao) {
        this.occasionDao = occasionDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<Occasion>> getAllOccasions(long userId) {
        return occasionDao.getAllOccasionsByUser(userId);
    }
    
    public LiveData<List<Occasion>> getOccasionsByPerson(long userId, long personId) {
        return occasionDao.getOccasionsByPerson(userId, personId);
    }
    
    public void insertOccasion(Occasion occasion, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = occasionDao.insert(occasion);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateOccasion(Occasion occasion, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                occasionDao.update(occasion);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteOccasion(Occasion occasion, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                occasionDao.delete(occasion);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getOccasionById(long id, RepositoryCallback<Occasion> callback) {
        executorService.execute(() -> {
            try {
                Occasion occasion = occasionDao.getOccasionById(id);
                callback.onSuccess(occasion);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getUpcomingOccasions(long userId, int days, RepositoryCallback<List<Occasion>> callback) {
        executorService.execute(() -> {
            try {
                LocalDate today = LocalDate.now();
                LocalDate endDate = today.plusDays(days);
                List<Occasion> occasions = occasionDao.getOccasionsByDateRange(
                    userId, 
                    today.toString(), 
                    endDate.toString()
                );
                callback.onSuccess(occasions);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getOccasionCount(long userId, int days, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                LocalDate today = LocalDate.now();
                LocalDate endDate = today.plusDays(days);
                int count = occasionDao.getOccasionCountByDateRange(
                    userId, 
                    today.toString(), 
                    endDate.toString()
                );
                callback.onSuccess(count);
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


