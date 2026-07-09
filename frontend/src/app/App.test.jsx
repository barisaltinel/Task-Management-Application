import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import App from './App';

vi.mock('../shared/utils/session', () => ({
  clearSession: vi.fn(),
  createSession: vi.fn(),
  readSession: () => null,
  saveSession: vi.fn(),
}));

vi.mock('../shared/api/client', () => ({
  apiRequest: vi.fn(),
}));

describe('App', () => {
  it('shows the authentication experience when there is no active session', () => {
    render(<App />);

    expect(
      screen.getByRole('heading', {
        name: /start your workspace with a calmer first step/i,
      })
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: /open workspace/i })
    ).toBeInTheDocument();
  });
});
