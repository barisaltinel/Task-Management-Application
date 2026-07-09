import { useCallback, useState } from 'react';
import {
  EMPTY_LOGIN_FORM,
  EMPTY_REGISTER_FORM,
} from '../config';
import {
  createSession,
  clearSession,
  readSession,
  saveSession,
} from '../../shared/utils/session';
import {
  loginUser,
  logoutUser,
  registerUser,
} from '../../shared/api/taskManagementApi';

export function useWorkspaceSession(showNotice) {
  const [session, setSession] = useState(readSession);
  const [authMode, setAuthMode] = useState('login');
  const [loginForm, setLoginForm] = useState(EMPTY_LOGIN_FORM);
  const [registerForm, setRegisterForm] = useState(EMPTY_REGISTER_FORM);
  const [loading, setLoading] = useState(false);

  const role = session?.user?.role || '';

  const clearActiveSession = useCallback(
    (message = 'Your session has expired. Please sign in again.') => {
      clearSession();
      setSession(null);
      setAuthMode('login');
      showNotice({
        type: 'info',
        title: 'Session ended',
        text: message,
      });
    },
    [showNotice]
  );

  const handleLogin = useCallback(
    async (event) => {
      event.preventDefault();
      try {
        setLoading(true);
        const authPayload = await loginUser(loginForm);
        const nextSession = createSession(authPayload, loginForm.email);
        saveSession(nextSession);
        setSession(nextSession);
        setLoginForm(EMPTY_LOGIN_FORM);
        showNotice({
          type: 'success',
          title: 'Signed in',
          text: 'Welcome back. Your workspace is syncing now.',
        });
      } catch (error) {
        showNotice({
          type: 'error',
          title: 'Sign-in failed',
          text: error.message || 'Sign in failed.',
        });
      } finally {
        setLoading(false);
      }
    },
    [loginForm, showNotice]
  );

  const handleRegister = useCallback(
    async (event) => {
      event.preventDefault();
      try {
        setLoading(true);
        await registerUser(registerForm);
        setAuthMode('login');
        setRegisterForm(EMPTY_REGISTER_FORM);
        showNotice({
          type: 'success',
          title: 'Account created',
          text: 'Please sign in to open your workspace.',
        });
      } catch (error) {
        showNotice({
          type: 'error',
          title: 'Registration failed',
          text: error.message || 'Registration failed.',
        });
      } finally {
        setLoading(false);
      }
    },
    [registerForm, showNotice]
  );

  const handleLogout = useCallback(() => {
    if (session?.token) {
      logoutUser(session.token).catch(() => {});
    }

    clearSession();
    setSession(null);
    setAuthMode('login');
    showNotice({
      type: 'info',
      title: 'Signed out',
      text: 'You have signed out of this tab.',
    });
  }, [session?.token, showNotice]);

  return {
    session,
    role,
    authMode,
    setAuthMode,
    loginForm,
    setLoginForm,
    registerForm,
    setRegisterForm,
    loading,
    handleLogin,
    handleRegister,
    handleLogout,
    clearActiveSession,
  };
}
