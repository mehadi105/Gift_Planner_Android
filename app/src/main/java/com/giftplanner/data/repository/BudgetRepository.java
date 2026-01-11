package com.giftplanner.data.repository;

import androidx.lifecycle.LiveData;
import com.giftplanner.data.dao.BudgetDao;
import com.giftplanner.data.dao.GiftHistoryDao;
import com.giftplanner.data.entity.Budget;
import com.giftplanner.util.DateFormatter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final GiftHistoryDao giftHistoryDao;
    private final ExecutorService executorService;
    
    public BudgetRepository(BudgetDao budgetDao, GiftHistoryDao giftHistoryDao) {
        this.budgetDao = budgetDao;
        this.giftHistoryDao = giftHistoryDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<Budget>> getAllBudgets(long userId) {
        return budgetDao.getAllBudgetsByUser(userId);
    }
    
    public void getBudgetByMonth(long userId, String month, RepositoryCallback<Budget> callback) {
        executorService.execute(() -> {
            try {
                Budget budget = budgetDao.getBudgetByMonth(userId, month);
                callback.onSuccess(budget);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertOrUpdateBudget(Budget budget, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                Budget existing = budgetDao.getBudgetByMonth(budget.getUserId(), budget.getMonth());
                if (existing != null) {
                    budget.setId(existing.getId());
                    budgetDao.update(budget);
                } else {
                    budgetDao.insert(budget);
                }
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateActualSpent(long userId, String month, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                String startDate = DateFormatter.getMonthStartDate(month);
                String endDate = DateFormatter.getMonthEndDate(month);
                Double totalSpent = giftHistoryDao.getTotalSpentByDateRange(userId, startDate, endDate);
                if (totalSpent == null) totalSpent = 0.0;
                
                Budget budget = budgetDao.getBudgetByMonth(userId, month);
                if (budget != null) {
                    budget.setActualSpent(totalSpent);
                    budgetDao.update(budget);
                }
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void calculateActualSpent(long userId, String month, RepositoryCallback<Double> callback) {
        executorService.execute(() -> {
            try {
                String startDate = DateFormatter.getMonthStartDate(month);
                String endDate = DateFormatter.getMonthEndDate(month);
                Double totalSpent = giftHistoryDao.getTotalSpentByDateRange(userId, startDate, endDate);
                callback.onSuccess(totalSpent != null ? totalSpent : 0.0);
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


