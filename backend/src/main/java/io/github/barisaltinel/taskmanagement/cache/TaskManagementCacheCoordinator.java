package io.github.barisaltinel.taskmanagement.cache;

public interface TaskManagementCacheCoordinator {
    void evictWorkspaceCaches();

    void evictUserCaches();

    static TaskManagementCacheCoordinator noOp() {
        return NoOpTaskManagementCacheCoordinator.INSTANCE;
    }

    final class NoOpTaskManagementCacheCoordinator implements TaskManagementCacheCoordinator {
        private static final NoOpTaskManagementCacheCoordinator INSTANCE = new NoOpTaskManagementCacheCoordinator();

        private NoOpTaskManagementCacheCoordinator() {
        }

        @Override
        public void evictWorkspaceCaches() {
        }

        @Override
        public void evictUserCaches() {
        }
    }
}
