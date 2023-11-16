import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'AuthContext'
import axios from 'axios'
import { Autocomplete } from '@react-google-maps/api'
import logo from '../../Logos/mainLogo.png'
import NavBar from 'components/Navbar/NavBar'

export const CustomerProfile = () => {
  const navigate = useNavigate()
  const { accessAcount } = useAuth()
  const [autoComplete, setAutoComplete] = useState(null)
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    type: 'CUSTOMER',
    accountInfo: {
      firstName: '',
      middleName: '',
      lastName: '',
      address: '',
      phoneNumber: '',
    },
  })
  const passwordsMatch = () => formData.password === formData.confirmPassword

  const handleConfirmPasswordChange = (e) => {
    const { value } = e.target
    // Update the confirmPassword field
    setFormData((prevData) => ({
      ...prevData,
      confirmPassword: value,
    }))
  }

  const handleChange = (e) => {
    const { name, value } = e.target

    // For nested objects (accountInfo), you need to spread them correctly
    if (name.includes('accountInfo.')) {
      const accountInfo = { ...formData.accountInfo }
      const field = name.split('.')[1]
      accountInfo[field] = value

      setFormData({
        ...formData,
        accountInfo: accountInfo,
      })
    } else {
      setFormData({
        ...formData,
        [name]: value,
      })
    }
  }

  const onPlaceChanged = () => {
    if (autoComplete) {
      const place = autoComplete.getPlace()
      const address = place.formatted_address
      setFormData({
        ...formData,
        accountInfo: {
          ...formData.accountInfo,
          address: address,
        },
      })
    }
  }

  const onLoad = (autoC) => setAutoComplete(autoC)

  async function save(event) {
    event.preventDefault()
    try {
      const { email, password, type, accountInfo } = formData
      await axios
        .post(`${process.env.REACT_APP_API_URL}/api/register`, {
          email: email,
          password: password,
          type: type,
          accountInfo: {
            firstName: accountInfo.firstName,
            middleName: accountInfo.middleName,
            lastName: accountInfo.lastName,
            address: accountInfo.address,
            phoneNumber: accountInfo.phoneNumber,
          },
        })
        .then(
          (res) => {
            console.log(res.data)
            alert(
              'Customer Registation Successful. Please Login to your New Account!',
            )
            navigate('/customer/login')
          },
          (fail) => {
            alert('Oops...an error occurred. Please try again.')
            console.error(fail) // Error!
          },
        )
    } catch (err) {
      // Handle other errors
      alert('An unexpected error occurred. Please try again.')
    }
  }

  return (
    <div className="bg-gradient-to-r from-[#b9a290] via-[#76323f] to-[#b9a290]">
      <NavBar />
      <div className="flex flex-col items-center  mx-auto md:h-screen pt-2">
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Edit Your Account
            </h1>
            <form className="space-y-4 " action="#" onSubmit={save}>
              <div className="flex justify-between">
                <input
                  type="text"
                  name="accountInfo.firstName"
                  value={formData.accountInfo.firstName}
                  onChange={handleChange}
                  placeholder="First Name"
                  required
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
                <div className="p-2"></div>
                <input
                  type="text"
                  name="accountInfo.lastName"
                  value={formData.accountInfo.lastName}
                  onChange={handleChange}
                  placeholder="Last Name"
                  required
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>

              <div>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Email"
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  required=""
                />
              </div>

              <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                <div>
                  <input
                    placeholder="Address"
                    required
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                </div>
              </Autocomplete>
              <div>
                <input
                  type="text"
                  name="accountInfo.phoneNumber"
                  value={formData.accountInfo.phoneNumber}
                  onChange={handleChange}
                  placeholder="Phone Number"
                  required
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
