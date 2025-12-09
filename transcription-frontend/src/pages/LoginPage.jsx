import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { loginUser } from '../api/authApi';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const royalBlue = '#002D62';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      
      const response = await loginUser(email, password);
      
      // Assume the backend returns the JWT in a 'token' or 'jwt' field
      const jwtToken = response.token || response.jwt;
      
      if (jwtToken) {
        login(jwtToken); // Store token and update auth state
        navigate('/dashboard'); // Redirect to protected area
      } else {
        setError('Login failed: Token not received.');
      }
    } catch (err) {
      // Handle server-side errors (e.g., bad credentials, validation errors)
      const errorMessage = err.message || 'Invalid credentials. Please check your email and password.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-4">
          <div className="card shadow-lg border-0 rounded-4">
            <div className="card-header text-center p-4" style={{ backgroundColor: royalBlue }}>
              <h4 className="text-white fw-bold mb-0">Secure Login</h4>
            </div>
            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="emailInput" className="form-label">Email Address</label>
                  <input
                    type="email"
                    className="form-control"
                    id="emailInput"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="mb-4">
                  <label htmlFor="passwordInput" className="form-label">Password</label>
                  <input
                    type="password"
                    className="form-control"
                    id="passwordInput"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                {error && (
                  <div className="alert alert-danger" role="alert">
                    {error}
                  </div>
                )}

                <button 
                  type="submit" 
                  className="btn btn-warning w-100 fw-bold shadow-sm"
                  disabled={loading}
                >
                  {loading ? (
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  ) : 'Login'}
                </button>
              </form>
            </div>
            <div className="card-footer text-center p-3 bg-light rounded-bottom-4">
              <small className="text-muted">
                Don't have an account? <Link to="/register" style={{ color: royalBlue, fontWeight: 'bold' }}>Register Here</Link>
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;