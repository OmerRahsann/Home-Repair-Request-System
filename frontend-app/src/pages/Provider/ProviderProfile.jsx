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
import { formatPhoneNumber, getServices } from 'Helpers/helpers'
import ProviderDescription from 'components/ServiceProviderHome/ProviderDescription'

export const ProviderProfile = () => {
  const navigate = useNavigate()
  const { accessAccount } = useAuth()
  const [autoComplete, setAutoComplete] = useState(null)
  const [edit, setEdit] = useState(false)
  const [services, setServices] = useState([])
  const [description, setDescription] = useState('')
  const [selectedServices, setSelectedServices] = useState([])
  const [formData, setFormData] = useState({
    contactEmailAddress: '',
    type: 'SERVICE_PROVIDER',
    accountInfo: {
      name: '',
      lastName: '',
      address: '',
      phoneNumber: '',
      services: [],
    },
  })

  function handleSelect(data) {
    setSelectedServices([data[0].value])
  }

  const handleDescriptionChange = (newDescription) => {
    setDescription(newDescription)
  }

  useEffect(() => {
    fetchServices()
    // Fetch provider profile data when the component mounts
    const fetchProviderData = async () => {
      try {
        const response = await axios.get(
          `${process.env.REACT_APP_API_URL}/api/account/provider`,
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
          contactEmailAddress,
          accountInfo: {
            name,
            description,
            services,
            phoneNumber,
            address,
          },
        })
      } catch (error) {
        console.error('Error fetching provider data:', error)
      }
    }

    fetchProviderData()
  }, [])

  async function fetchServices() {
    try {
      const services = await getServices()
      setServices(services)
    } catch (error) {
      // Handle the error here
      console.error('Error fetching services:', error)
    }
  }

  const handleUpdate = async (event) => {
    event.preventDefault()
    try {
      const { accountInfo } = formData
      console.log(formData)
      await axios.post(
        `${process.env.REACT_APP_API_URL}/api/account/provider/update`,
        {
          name: accountInfo.name,
          description:
            description.length > 0 ? description : accountInfo.description,
          services:
            selectedServices.length > 0
              ? selectedServices
              : accountInfo.services,
          phoneNumber: accountInfo.phoneNumber.replace(/\D/g, ''),
          address: accountInfo.address,
          contactEmailAddress: formData.contactEmailAddress,
        },
        {
          withCredentials: true,
        },
      )
      alert('Provider information updated successfully.')
      setEdit(false)
    } catch (error) {
      console.error('Error updating provider information:', error)
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
    if (name.includes('accountInfo.phoneNumber')) {
      const formattedPhoneNumber = formatPhoneNumber(value)
      setFormData({
        ...formData,
        accountInfo: {
          ...formData.accountInfo,
          phoneNumber: formattedPhoneNumber,
        },
      })
    } else if (name.includes('accountInfo.')) {
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
            {formData.accountInfo.name}
          </h1>
          <div className="flex flex-col items-center justify-between mt-2">
            <p>📧 {formData.contactEmailAddress}</p>
            <p>📞 {formData.accountInfo.phoneNumber}</p>
            <p>📍 {formData.accountInfo.address}</p>
          </div>
          <div className="text-center mt-2">
            {formData.accountInfo.description}
          </div>
          <div className="flex flex-col items-center justify-center mt-2">
            <label className="font-bold">Your Services: </label>
            <ul className="list-disc ml-2">
              {formData.accountInfo.services?.map((service, index) => (
                <li key={index}>{service}</li>
              ))}
            </ul>
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
                    name="accountInfo.name"
                    onChange={handleChange}
                    placeholder={formData.accountInfo.name}
                    required
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                </div>

                <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                  <div>
                    <input
                      placeholder={formData.accountInfo.address}
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
                    placeholder={formData.accountInfo.phoneNumber}
                    className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  />
                </div>
                <ProviderDescription
                  onDescriptionChange={handleDescriptionChange}
                />
                <Select
                  onChange={handleSelect}
                  isMulti
                  className="bg-custom-gray"
                  options={services}
                  placeholder={formData.accountInfo.services[0]}
                />
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
