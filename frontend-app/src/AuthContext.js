// AuthContext.js
import React, { createContext, useContext, useState } from 'react';
import axios from 'axios';

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
    axios.get("http://localhost:8080/api/account/type", {withCredentials: true})
            .then((res) => {
            const userType = res.data
            return userType === "SERVICE_PROVIDER"
            })
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


  export async function checkIsServiceProviderLoggedIn() {
    try {
      const response = await axios.get("http://localhost:8080/api/account/type", { withCredentials: true });
      const type = response.data;
      console.log(type );
      if(type === "SERVICE_PROVIDER") return true
      else return false
    } catch (error) {
      console.error(error);
      return null; // or some other appropriate error handling
    }
  }

  export async function checkIsCustomerLoggedIn() {
    try {
      const response = await axios.get("http://localhost:8080/api/account/type", { withCredentials: true });
      const type = response.data;
      console.log(type );
      if(type === "CUSTOMER") return true
    } catch (error) {
      console.error(error);
      return false; // or some other appropriate error handling
    }
  }

export function logout() {
    localStorage.removeItem('isLoggedIn');
    // Clear any other user-related data as well
  }
