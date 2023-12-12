// AuthContext.js
import React, { createContext, useContext, useState } from 'react'
import axios from 'axios'

const AuthContext = createContext()

export function useAuth() {
  return useContext(AuthContext)
}

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  const accessServiceProviderAccount = (type) => {
    axios
      .get('/api/account/type', { withCredentials: true })
      .then((res) => {
        const userType = res.data
        return userType === 'SERVICE_PROVIDER'
      })
  }

  const logout = () => {
    // Implement your logout logic here
    setIsLoggedIn(false)
  }

  const value = {
    isLoggedIn,
    logout,
    accessServiceProviderAccount,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function checkIsLoggedIn() {
  const isLoggedIn = localStorage.getItem('isLoggedIn')
  return isLoggedIn === 'true'
}

export async function checkIsServiceProviderLoggedIn() {
  try {
    const response = await axios.get(
      '/api/account/type',
      { withCredentials: true },
    )
    const type = response.data
    console.log(type)
    return type === 'SERVICE_PROVIDER'
  } catch (error) {
    console.error(error)
    return null // or some other appropriate error handling
  }
}

export async function checkIsCustomerLoggedIn() {
  try {
    const response = await axios.get(
      '/api/account/type',
      { withCredentials: true },
    )
    const type = response.data
    console.log(type)
    return type === 'CUSTOMER'
  } catch (error) {
    console.error(error)
    console.log(false)
    return false // or some other appropriate error handling
  }
}

export function logout() {
  axios.get('/api/logout', {
    withCredentials: true,
  })
  alert('You have now been Logged Out. ')
  // Clear any other user-related data as well
}
