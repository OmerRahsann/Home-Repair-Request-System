import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'AuthContext'
import axios from 'axios'
import { Autocomplete } from '@react-google-maps/api'
import Navbar from '../../components/Navbar/NavBar'
import ServiceRequestModal from '../../components/Customer/ServiceRequestModal'
import { formatPhoneNumber } from 'Helpers/helpers'

function createBlankData() {
  return {
    firstName: '',
    middleName: '',
    lastName: '',
    address: '',
    phoneNumber: '',
  }
}

export const CustomerProfile = () => {
  const navigate = useNavigate()
  const { isLoggedIn, userType, checkAuth } = useAuth()
  const [autoComplete, setAutoComplete] = useState(null)
  const [edit, setEdit] = useState(false)
  const [email, setEmail] = useState('')

  const [currentData, setCurrentData] = useState(createBlankData())
  const [formData, setFormData] = useState(createBlankData())

  useEffect(checkAuth, [isLoggedIn, userType])
  useEffect(() => {
    if (isLoggedIn == false) {
      navigate('/customer/login')
    } else if (isLoggedIn && userType != 'CUSTOMER') {
      navigate('/provider/myprofile')
    }
  }, [isLoggedIn, userType])

  useEffect(() => {
    // Fetch customer profile data when the component mounts
    fetchCustomerData()
  }, [])

  useEffect(() => {
    // Fetch customer profile data when the component mounts
    const controller = new AbortController()
    axios
      .get('/api/account/email', {
        signal: controller.signal,
        withCredentials: true,
      })
      .then((response) => setEmail(response.data))
      .catch(() => {})
    return () => controller.abort()
  }, [email])

  const fetchCustomerData = async () => {
    try {
      const response = await axios.get('/api/account/customer', {
        withCredentials: true,
      })

      const { firstName, lastName, phoneNumber, address } = response.data
      setCurrentData({
        firstName,
        lastName,
        phoneNumber,
        address,
      })
      // Reset the form as the information may have changed
      setFormData(createBlankData())
    } catch (error) {
      console.error('Error fetching customer data:', error)
    }
  }

  const handleUpdate = async (event) => {
    event.preventDefault()

    try {
      let requestBody = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber.replace(/\D/g, ''),
        address: formData.address,
      }
      // Fill with previous data if an input field was left unfilled
      for (const [key, value] of Object.entries(requestBody)) {
        if (value.length == 0) {
          requestBody[key] = currentData[key]
        }
      }
      // We don't really care about the middle name
      requestBody.middleName = ''

      console.log(requestBody)
      await axios.post('/api/account/customer/update', requestBody, {
        withCredentials: true,
      })
      // Fetch the updated data
      fetchCustomerData()
      alert('Customer information updated successfully.')
      setEdit(false)
    } catch (error) {
      console.error('Error updating customer information:', error)

      if (
        error.response &&
        error.response.data &&
        error.response.data.type === 'validation_error'
      ) {
        // Handle validation errors
        const { fieldErrors } = error.response.data
        const errorMessage = fieldErrors
          .map((error) => `${error.field}: ${error.message}`)
          .join('\n')
        alert(errorMessage)
      } else {
        // Handle other types of errors
        alert(
          'An error occurred while updating customer information. Please try again.',
        )
      }
    }
  }

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
    if (name.includes('accountInfo.phoneNumber')) {
      const formattedPhoneNumber = formatPhoneNumber(value)
      setFormData({
        ...formData,
        phoneNumber: formattedPhoneNumber,
      })
    } else if (name.includes('accountInfo.')) {
      const newFormData = { ...formData }
      const field = name.split('.')[1]
      newFormData[field] = value

      setFormData(newFormData)
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
        address: address,
      })
    }
  }

  const onLoad = (autoC) => setAutoComplete(autoC)

  return (
    <div className="bg-custom-gray">
      <Navbar isLoggedIn={isLoggedIn} />
      <div className="bg-custom-gray h-screen flex flex-col justify-center items-center pb-44">
        <div className="w-full bg-white shadow-lg rounded-md p-4 md:w-2/5">
          <h1 className="font-bold text-2xl text-center">
            {currentData.lastName}, {currentData.firstName}
          </h1>
          <div className="flex flex-col items-center justify-between mt-2">
            <p>📧 {email}</p>
            <p>📞 {formatPhoneNumber(currentData.phoneNumber)}</p>
            <p>📍{currentData.address}</p>
          </div>
          <div className="flex flex-col md:flex-row justify-between mt-2">
            <button
              onClick={() => setEdit(true)}
              className="text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center md:mr-2 dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
            >
              Edit Account
            </button>
            <a
              className="mt-2 md:mt-0 text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
              href="/reset_password"
            >
              Reset Password
            </a>
          </div>
        </div>
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 p">
          <ServiceRequestModal isVisible={edit} onClose={() => setEdit(false)}>
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
                Edit Your Account
              </h1>
              <form className="space-y-4 " action="#" onSubmit={handleUpdate}>
                <div className="flex justify-between">
                  <input
                    type="text"
                    name="accountInfo.firstName"
                    onChange={handleChange}
                    placeholder={currentData.firstName}
                    required
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                  <div className="p-2"></div>
                  <input
                    type="text"
                    name="accountInfo.lastName"
                    onChange={handleChange}
                    placeholder={currentData.lastName}
                    required
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                </div>
                <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                  <div>
                    <input
                      placeholder={currentData.address}
                      required
                      className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                    />
                  </div>
                </Autocomplete>
                <div>
                  <input
                    type="text"
                    name="accountInfo.phoneNumber"
                    onChange={handleChange}
                    value={formData.phoneNumber}
                    placeholder={formatPhoneNumber(currentData.phoneNumber)}
                    required
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                </div>
              </form>
              <button
                onClick={handleUpdate}
                className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center md:mr-2 dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
              >
                Update
              </button>
            </div>
          </ServiceRequestModal>
        </div>
      </div>
    </div>
  )
}
