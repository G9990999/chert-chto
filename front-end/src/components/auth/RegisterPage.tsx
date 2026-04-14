import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../../services/api';
import { useAuthStore } from '../../store/authStore';

/**
 * Registration page component.
 * On success, stores the JWT and navigates to the pages list.
 */
export function RegisterPage() {
  const navigate = useNavigate();
  const loginStore = useAuthStore((s) => s.login);
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    displayName: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await register(form);
      loginStore(data);
      navigate('/pages');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
      setError(msg ?? 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>MWS Wiki</h1>
        <h2>Create Account</h2>
        <form onSubmit={handleSubmit}>
          {(['username', 'email', 'password', 'displayName'] as const).map(
            (field) => (
              <div className="form-group" key={field}>
                <label htmlFor={field}>
                  {field === 'displayName'
                    ? 'Display Name (optional)'
                    : field.charAt(0).toUpperCase() + field.slice(1)}
                </label>
                <input
                  id={field}
                  name={field}
                  type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'}
                  value={form[field]}
                  onChange={handleChange}
                  required={field !== 'displayName'}
                  minLength={field === 'password' ? 8 : field === 'username' ? 3 : undefined}
                />
              </div>
            )
          )}
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>
        <p className="auth-link">
          Already have an account? <Link to="/login">Sign In</Link>
        </p>
      </div>
    </div>
  );
}
