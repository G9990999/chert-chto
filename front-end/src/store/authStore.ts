import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthResponse, Role } from '../types';

interface AuthState {
  token: string | null;
  userId: string | null;
  username: string | null;
  role: Role | null;
  displayName: string | null;
  isAuthenticated: boolean;
  login: (data: AuthResponse) => void;
  logout: () => void;
}

/**
 * Zustand store for authentication state.
 * Persisted to localStorage so the user stays logged in on refresh.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      username: null,
      role: null,
      displayName: null,
      isAuthenticated: false,

      login: (data: AuthResponse) => set({
        token: data.token,
        userId: data.userId,
        username: data.username,
        role: data.role,
        displayName: data.displayName,
        isAuthenticated: true,
      }),

      logout: () => set({
        token: null,
        userId: null,
        username: null,
        role: null,
        displayName: null,
        isAuthenticated: false,
      }),
    }),
    {
      name: 'mws-wiki-auth',
    }
  )
);
