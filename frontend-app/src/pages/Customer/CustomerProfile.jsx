import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'AuthContext'
import axios from 'axios'
import { Autocomplete } from '@react-google-maps/api'
import logo from '../../Logos/mainLogo.png'
import NavBarProvider from 'components/Navbar/NavBarProvider'
import Select from 'react-select'
import Review from '../../components/ServiceProviderHome/Review'
import ServiceRequestModal from '../../components/Customer/ServiceRequestModal'

export const CustomerProfile = () => {
  const navigate = useNavigate()
  const { accessAccount } = useAuth()
  const [autoComplete, setAutoComplete] = useState(null)
  const [edit, setEdit] = useState(false)
  const [formData, setFormData] = useState({
    email: '',
    type: 'CUSTOMER',
    accountInfo: {
      firstName: '',
      middleName: '',
      lastName: '',
      address: '',
      phoneNumber: '',
    },
  })

  useEffect(() => {
    // Fetch customer profile data when the component mounts
    const fetchCustomerData = async () => {
      try {
        const response = await axios.get(
          `${process.env.REACT_APP_API_URL}/api/account/customer`,
          {
            withCredentials: true,
          },
        )

        const {
          name,
          description,
          services,
          phoneNumber,
          address,
          contactEmailAddress,
        } = response.data
        setFormData({
          ...formData,
          accountInfo: {
            name,
            phoneNumber,
            address,
            contactEmailAddress,
          },
        })
      } catch (error) {
        console.error('Error fetching customer data:', error)
      }
    }

    fetchCustomerData()
  }, [])

  const handleUpdate = async (event) => {
    event.preventDefault()
    try {
      const { accountInfo } = formData
      await axios.post(
        `${process.env.REACT_APP_API_URL}/api/account/customer/update`,
        {
          name: accountInfo.name,
          phoneNumber: accountInfo.phoneNumber,
          address: accountInfo.address,
        },
        {
          withCredentials: true,
        },
      )
      alert('Customer information updated successfully.')
      setEdit(false)
    } catch (error) {
      console.error('Error updating customer information:', error)
      alert(
        'An error occurred while updating provider information. Please try again.',
      )
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

  return (
    <div className="bg-custom-gray">
      <NavBarProvider />
      <div className="bg-custom-gray h-screen flex flex-col justify-center items-center pb-44">
        <div className="w-full bg-white shadow-lg rounded-md p-4 md:w-2/5">
          <h1 className="font-bold text-2xl text-center">
            {formData.accountInfo.lastName}, {formData.accountInfo.firstName}
          </h1>
          <div className="flex flex-col items-center justify-between mt-2">
            <p>📧 {formData.email}</p>
            <p>📞 {formData.accountInfo.phoneNumber}</p>
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
                <Select isMulti className="bg-custom-gray" />
              </form>
            </div>
          </ServiceRequestModal>
        </div>
      </div>
    </div>
  )
}
