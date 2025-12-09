// src/pages/RegisterPage.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api/authApi';

const RegisterPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  const navigate = useNavigate();
  const royalBlue = '#002D62';
  const saffron = '#FF9933';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);

    try {
      await registerUser({ email, password });
      
      setSuccess('Registration successful! You can now log in.');
      // Optionally redirect after a short delay
      setTimeout(() => navigate('/login'), 2000);
      
    } catch (err) {
      // Assuming backend returns validation/error details
      const errorMessage = err.message || 'Registration failed. Please check input fields.';
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
              <h4 className="text-white fw-bold mb-0">Create Account</h4>
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
                  <div className="alert alert-danger" role="alert">{error}</div>
                )}
                {success && (
                  <div className="alert alert-success" role="alert">{success}</div>
                )}

                <button 
                  type="submit" 
                  className="btn w-100 fw-bold shadow-sm"
                  style={{ backgroundColor: saffron, color: royalBlue }}
                  disabled={loading}
                >
                  {loading ? (
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  ) : 'Register'}
                </button>
              </form>
            </div>
            <div className="card-footer text-center p-3 bg-light rounded-bottom-4">
              <small className="text-muted">
                Already have an account? <Link to="/login" style={{ color: royalBlue, fontWeight: 'bold' }}>Login Here</Link>
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;