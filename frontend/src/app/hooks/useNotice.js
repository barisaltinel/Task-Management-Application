import { useCallback, useEffect, useState } from 'react';

export function useNotice() {
  const [notice, setNotice] = useState({ type: '', text: '', title: '' });

  const showNotice = useCallback((nextNotice) => {
    setNotice((currentNotice) => ({
      title: '',
      ...currentNotice,
      ...nextNotice,
    }));
  }, []);

  const dismissNotice = useCallback(() => {
    setNotice({ type: '', text: '', title: '' });
  }, []);

  useEffect(() => {
    if (!notice?.text || notice.type === 'error') {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      dismissNotice();
    }, 4200);

    return () => window.clearTimeout(timeoutId);
  }, [dismissNotice, notice]);

  return {
    notice,
    showNotice,
    dismissNotice,
  };
}
