
import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {

  const [token, setToken] = useState(localStorage.getItem('jwtToken'));
  const [isAuthenticated, setIsAuthenticated] = useState(!!token);

  
  useEffect(() => {
    const storedToken = localStorage.getItem('jwtToken');
    setToken(storedToken);
    setIsAuthenticated(!!storedToken);
  }, []);


  const login = (jwtToken) => {
    localStorage.setItem('jwtToken', jwtToken);
    setToken(jwtToken);
    setIsAuthenticated(true);
    
  };

  const logout = () => {
    localStorage.removeItem('jwtToken');
    setToken(null);
    setIsAuthenticated(false);
  };

  const value = {
    token,
    isAuthenticated,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};