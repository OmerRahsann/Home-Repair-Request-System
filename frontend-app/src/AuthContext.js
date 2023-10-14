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

  const accessServiceProviderAccount = (type) => {
    setIsLoggedIn(true);
    localStorage.setItem('isLoggedIn' , 'true');
    localStorage.setItem('type', type)
  }

  const logout = () => {
    // Implement your logout logic here
    setIsLoggedIn(false);
  };

  const value = {
    isLoggedIn,
    accessAcount,
    logout,
    accessServiceProviderAccount
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


export function checkIsServiceProviderLoggedIn() {
  const isLoggedIn = localStorage.getItem('isLoggedIn');
  const isServiceProvider = localStorage.getItem('type');
  return isLoggedIn === 'true' && isServiceProvider === 'SERVICE_PROVIDER';
}

export function logout() {
    localStorage.removeItem('isLoggedIn');
    // Clear any other user-related data as well
  }
