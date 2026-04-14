import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './components/auth/LoginPage';
import { RegisterPage } from './components/auth/RegisterPage';
import { PagesList } from './components/pages/PagesList';
import { PageView } from './components/pages/PageView';
import { useAuthStore } from './store/authStore';
import './styles.css';

/**
 * Route guard — redirects unauthenticated users to /login.
 */
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

/**
 * Application shell with routing.
 */
function App() {
  const { isAuthenticated, username, logout } = useAuthStore();

  return (
    <BrowserRouter>
      <div className="app">
        {isAuthenticated && (
          <nav className="navbar">
            <a href="/pages" className="nav-brand">MWS Wiki</a>
            <div className="nav-right">
              <span className="nav-user">{username}</span>
              <button onClick={logout} className="btn-logout">Sign Out</button>
            </div>
          </nav>
        )}
        <main className="main-content">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/pages"
              element={
                <PrivateRoute>
                  <PagesList />
                </PrivateRoute>
              }
            />
            <Route
              path="/pages/:id"
              element={
                <PrivateRoute>
                  <PageView />
                </PrivateRoute>
              }
            />
            <Route path="*" element={<Navigate to="/pages" replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

const root = createRoot(document.getElementById('root')!);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
