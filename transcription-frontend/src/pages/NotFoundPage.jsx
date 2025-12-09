
import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  const royalBlue = '#002D62';
  const saffron = '#FF9933';

  return (
    <div className="d-flex align-items-center justify-content-center vh-100" style={{ paddingTop: '56px' }}>
      <div className="container text-center">
        <div className="card shadow-lg p-5 border-0 rounded-4">
          <div className="card-body">
            <h1 className="display-1 fw-bold" style={{ color: saffron }}>404</h1>
            <h2 className="mb-4" style={{ color: royalBlue }}>Page Not Found</h2>
            <p className="lead text-muted mb-4">
              The page you are looking for doesn't exist or an error occurred.
            </p>
            <Link 
              to="/" 
              className="btn btn-lg fw-bold shadow-sm"
              style={{ backgroundColor: royalBlue, color: 'white' }}
            >
              Go to Homepage
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;