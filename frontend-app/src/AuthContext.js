// AuthContext.js
import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const accessAcount = () => {
    // Implement your login logic here
    setIsLoggedIn(true);
    localStorage.setItem('isLoggedIn', 'true');
  };

  const logout = () => {
    // Implement your logout logic here
    setIsLoggedIn(false);
  };

  const value = {
    isLoggedIn,
    accessAcount,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function checkIsLoggedIn() {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    return isLoggedIn === 'true';
  }

export function logout() {
    localStorage.removeItem('isLoggedIn');
    // Clear any other user-related data as well
  }
