// src/pages/LandingPage.jsx
import React from 'react';
import { Link } from 'react-router-dom';

const LandingPage = () => {
  const royalBlue = '#002D62';
  const saffron = '#FF9933';

  return (
    <div 
      className="d-flex align-items-center justify-content-center text-center vh-100"
      style={{ 
        backgroundColor: royalBlue, 
        paddingTop: '56px' // Account for fixed navbar height
      }}
    >
      <div className="container py-5">
        <h1 
          className="display-3 fw-bold mb-3" 
          style={{ color: 'white', textShadow: '2px 2px 4px rgba(0,0,0,0.3)' }}
        >
          $Bhaasha AI: Transform Voice to Insight
        </h1>
        <p className="lead text-white-50 mb-5">
          Secure, enterprise-grade transcription and analysis powered by Spring Boot.
        </p>
        <div className="d-grid gap-3 d-sm-flex justify-content-sm-center">
          <Link 
            to="/login" 
            className="btn btn-lg fw-bold px-4 shadow-lg"
            style={{ backgroundColor: saffron, color: royalBlue, borderColor: saffron }}
          >
            Login to Dashboard
          </Link>
          <Link 
            to="/register" 
            className="btn btn-outline-light btn-lg fw-bold px-4"
          >
            Create Account
          </Link>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;