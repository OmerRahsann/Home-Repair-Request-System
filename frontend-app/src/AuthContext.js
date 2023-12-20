// AuthContext.js
import React, { createContext, useContext, useState } from 'react'
import axios from 'axios'

const AuthContext = createContext()

export function useAuth() {
  return useContext(AuthContext)
}

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(undefined)
  const [userType, setUserType] = useState(undefined)

  const checkAuth = () => {
    const controller = new AbortController()
    axios
      .get('/api/account/type', {
        signal: controller.signal,
        withCredentials: true,
      })
      .then((res) => {
        const userType = res.data
        setUserType(userType)
        setIsLoggedIn(true)
      })
      .catch((error) => {
        if (error.response && error.response.status == 403) {
          setIsLoggedIn(false)
        }
      })
    return () => controller.abort()
  }

  const value = {
    isLoggedIn,
    userType,
    checkAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export async function checkIsServiceProviderLoggedIn() {
  try {
    const response = await axios.get('/api/account/type', {
      withCredentials: true,
    })
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
    const response = await axios.get('/api/account/type', {
      withCredentials: true,
    })
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
