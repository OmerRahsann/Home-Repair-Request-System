import React, { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import logo from '../../Logos/mainLogo.png'
import { Autocomplete } from '@react-google-maps/api'

function CustomerSignUp() {
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
        .post('http://localhost:8080/api/register', {
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
            alert('An account with that email already exists.')
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
      <a href="/">
        <img className="inset-y-0 h-28" src={logo} alt="logo" />
      </a>
      <a
        href="/provider/login"
        className="text-blue-700 hover:underlin absolute top-0 right-0 pr-2 font-bold"
      >
        Are you a service provider?
      </a>
      <div className="flex flex-col items-center  mx-auto md:h-screen ">
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Create a Customer Account
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
              <div>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Password"
                  pattern=".{8,}"
                  required
                  title="Password must be at least 8 characters long."
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>
              <div>
                <input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleConfirmPasswordChange}
                  placeholder="Confirm Password"
                  required
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>
              {formData.confirmPassword && (
                <span
                  className={
                    passwordsMatch() ? 'text-green-500' : 'text-red-500'
                  }
                >
                  {passwordsMatch()
                    ? '✓ Passwords match'
                    : '✗ Passwords do not match'}
                </span>
              )}
              <button
                type="submit"
                disabled={!passwordsMatch()}
                className={`${
                  passwordsMatch()
                    ? 'text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'
                    : 'cursor-not-allowed w-full bg-gray-200 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800 text-white'
                }`}
              >
                Sign Up
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CustomerSignUp
