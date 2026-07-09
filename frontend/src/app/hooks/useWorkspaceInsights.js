import { useMemo } from 'react';

export function useWorkspaceInsights(workspace) {
  const selectableUsers = useMemo(() => {
    const usersById = new Map();

    const registerUser = (user) => {
      if (!user?.id || usersById.has(user.id)) {
        return;
      }

      usersById.set(user.id, user);
    };

    workspace.projects.forEach((project) => {
      (project.teamMembers || []).forEach(registerUser);
    });
    workspace.tasks.forEach((task) => registerUser(task.assignee));

    return Array.from(usersById.values()).sort((left, right) =>
      (left.name || '').localeCompare(right.name || '', undefined, {
        sensitivity: 'base',
      })
    );
  }, [workspace.projects, workspace.tasks]);

  const projectSnapshots = useMemo(
    () =>
      workspace.projects
        .map((project) => {
          const projectTasks = workspace.tasks.filter(
            (task) => task.project?.id === project.id
          );
          const completedCount = projectTasks.filter(
            (task) => task.state === 'COMPLETED'
          ).length;
          const blockedCount = projectTasks.filter(
            (task) => task.state === 'BLOCKED'
          ).length;
          const criticalCount = projectTasks.filter(
            (task) => task.priority === 'CRITICAL' && task.state !== 'COMPLETED'
          ).length;

          return {
            ...project,
            taskCount: projectTasks.length,
            completedCount,
            blockedCount,
            criticalCount,
            teamCount: project.teamMembers?.length || 0,
            completionRate: projectTasks.length
              ? Math.round((completedCount / projectTasks.length) * 100)
              : 0,
          };
        })
        .sort((left, right) => right.taskCount - left.taskCount),
    [workspace.projects, workspace.tasks]
  );

  const metrics = useMemo(
    () => ({
      total: workspace.tasks.length,
      done: workspace.tasks.filter((task) => task.state === 'COMPLETED').length,
      blocked: workspace.tasks.filter((task) => task.state === 'BLOCKED').length,
      critical: workspace.tasks.filter((task) => task.priority === 'CRITICAL')
        .length,
      activeProjects: workspace.projects.filter(
        (project) => project.status === 'IN_PROGRESS'
      ).length,
    }),
    [workspace.projects, workspace.tasks]
  );

  const overview = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const openTasks = workspace.tasks.filter(
      (task) => !['COMPLETED', 'CANCELLED'].includes(task.state)
    );
    const scheduledOpenTasks = openTasks
      .filter((task) => task.dueDate)
      .sort(
        (left, right) =>
          new Date(`${left.dueDate}T00:00:00`).getTime() -
          new Date(`${right.dueDate}T00:00:00`).getTime()
      );
    const overdueTasks = scheduledOpenTasks.filter(
      (task) => new Date(`${task.dueDate}T00:00:00`).getTime() < today.getTime()
    );
    const dueThisWeekCount = scheduledOpenTasks.filter((task) => {
      const dueTime = new Date(`${task.dueDate}T00:00:00`).getTime();
      const diffInDays = Math.round((dueTime - today.getTime()) / 86400000);
      return diffInDays >= 0 && diffInDays <= 7;
    }).length;
    const atRiskTasks = workspace.tasks
      .filter((task) => {
        if (task.state === 'BLOCKED' || task.state === 'CANCELLED') {
          return true;
        }

        if (task.priority === 'CRITICAL' && task.state !== 'COMPLETED') {
          return true;
        }

        if (!task.dueDate || ['COMPLETED', 'CANCELLED'].includes(task.state)) {
          return false;
        }

        return new Date(`${task.dueDate}T00:00:00`).getTime() < today.getTime();
      })
      .sort((left, right) => {
        const resolveWeight = (task) => {
          if (task.state === 'BLOCKED') {
            return 4;
          }
          if (
            task.dueDate &&
            !['COMPLETED', 'CANCELLED'].includes(task.state) &&
            new Date(`${task.dueDate}T00:00:00`).getTime() < today.getTime()
          ) {
            return 3;
          }
          if (task.priority === 'CRITICAL' && task.state !== 'COMPLETED') {
            return 2;
          }
          return 1;
        };

        return resolveWeight(right) - resolveWeight(left);
      });
    const completionRate = metrics.total
      ? Math.round((metrics.done / metrics.total) * 100)
      : 0;
    const openWorkRate = metrics.total
      ? Math.round((openTasks.length / metrics.total) * 100)
      : 0;
    const collaborationVolume =
      workspace.comments.length + workspace.attachments.length;
    const teamLoad = selectableUsers
      .map((user) => {
        const assignedTasks = workspace.tasks.filter(
          (task) => task.assignee?.id === user.id
        );
        const activeCount = assignedTasks.filter(
          (task) => !['COMPLETED', 'CANCELLED'].includes(task.state)
        ).length;

        return {
          id: user.id,
          name: user.name,
          role: user.role,
          totalCount: assignedTasks.length,
          activeCount,
          blockedCount: assignedTasks.filter((task) => task.state === 'BLOCKED')
            .length,
          criticalCount: assignedTasks.filter(
            (task) => task.priority === 'CRITICAL' && task.state !== 'COMPLETED'
          ).length,
        };
      })
      .filter((entry) => entry.totalCount > 0)
      .sort((left, right) => right.activeCount - left.activeCount)
      .slice(0, 5);
    const recentComments = [...workspace.comments]
      .sort(
        (left, right) =>
          new Date(right.createdAt).getTime() -
          new Date(left.createdAt).getTime()
      )
      .slice(0, 4);
    const recentFiles = [...workspace.attachments]
      .sort(
        (left, right) =>
          new Date(right.uploadedAt).getTime() -
          new Date(left.uploadedAt).getTime()
      )
      .slice(0, 4);

    let pulseLabel = 'Building momentum';
    if (
      completionRate >= 70 &&
      metrics.blocked <= 1 &&
      overdueTasks.length === 0
    ) {
      pulseLabel = 'Healthy delivery pace';
    } else if (
      overdueTasks.length >= 2 ||
      atRiskTasks.length >= 4 ||
      metrics.blocked >= 3
    ) {
      pulseLabel = 'Needs leadership attention';
    }

    return {
      completionRate,
      openWorkRate,
      collaborationVolume,
      atRiskTasks: atRiskTasks.slice(0, 5),
      atRiskCount: atRiskTasks.length,
      overdueCount: overdueTasks.length,
      dueThisWeekCount,
      upcomingDeadlines: scheduledOpenTasks.slice(0, 6),
      teamLoad,
      recentComments,
      recentFiles,
      projectSnapshots: projectSnapshots.slice(0, 4),
      pulseLabel,
    };
  }, [
    metrics,
    projectSnapshots,
    selectableUsers,
    workspace.attachments,
    workspace.comments,
    workspace.tasks,
  ]);

  return {
    selectableUsers,
    projectSnapshots,
    metrics,
    overview,
  };
}
