package io.github.barisaltinel.taskmanagement.cache;

import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import java.util.Locale;
import org.springframework.util.StringUtils;

public final class TaskManagementCacheKeys {
    private TaskManagementCacheKeys() {}

    public static String currentAccessScope() {
        String username = SecurityUtils.getCurrentUsername();
        String normalizedUsername =
                StringUtils.hasText(username) ? username.trim().toLowerCase(Locale.ROOT) : "anonymous";
        String scope =
                SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER") ? "privileged" : "restricted";
        return scope + ":" + normalizedUsername;
    }

    public static String scopedId(Object id) {
        return currentAccessScope() + ":" + id;
    }
}
