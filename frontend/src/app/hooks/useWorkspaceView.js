import { useEffect, useState } from 'react';
import {
  DEFAULT_VIEW,
  getAvailableViews,
  getDefaultViewForRole,
} from '../config';

export function useWorkspaceView(role, canManageProjects) {
  const [view, setView] = useState(DEFAULT_VIEW);
  const availableViews = getAvailableViews(role, canManageProjects);

  useEffect(() => {
    if (!availableViews.includes(view)) {
      setView(getDefaultViewForRole(role, canManageProjects));
    }
  }, [availableViews, canManageProjects, role, view]);

  return {
    view,
    setView,
    availableViews,
  };
}
