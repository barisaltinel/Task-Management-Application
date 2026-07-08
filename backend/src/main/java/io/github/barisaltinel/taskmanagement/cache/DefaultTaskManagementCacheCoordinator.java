package io.github.barisaltinel.taskmanagement.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class DefaultTaskManagementCacheCoordinator implements TaskManagementCacheCoordinator {
    private final CacheManager cacheManager;

    public DefaultTaskManagementCacheCoordinator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void evictWorkspaceCaches() {
        runAfterCommit(() -> TaskManagementCacheNames.workspace().forEach(this::clearCache));
    }

    @Override
    public void evictUserCaches() {
        runAfterCommit(() -> {
            clearCache(TaskManagementCacheNames.USER_LIST);
            clearCache(TaskManagementCacheNames.USER_DETAILS);
        });
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }
}
