import React, { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import logo from '../../Logos/mainLogo.png'
import ProviderDescription from '../../components/ServiceProviderHome/ProviderDescription'
import Select from 'react-select'
import { Autocomplete } from '@react-google-maps/api'
import { formatPhoneNumber } from 'Helpers/helpers'

function ProviderSignUp() {
  const navigate = useNavigate()
  const { accessServiceProviderAccount } = useAuth()
  const services = [
    { value: 'plumbing', label: 'Plumbing' },
    { value: 'yardwork', label: 'Yardwork' },
    { value: 'roofing', label: 'Roofing' },
  ]
  const [selectedServices, setSelectedServices] = useState()
  const [description, setDescription] = useState('')
  const [autoComplete, setAutoComplete] = useState(null)

  // Callback function to set the description in the parent component's state
  const handleDescriptionChange = (newDescription) => {
    setDescription(newDescription)
  }
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    type: 'SERVICE_PROVIDER',
    accountInfo: {
      name: '',
      description: '',
      services: [],
      phoneNumber: '',
      address: '',
      contactEmailAddress: '',
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

  function handleSelect(data) {
    setSelectedServices(data)
  }

  async function save(event) {
    event.preventDefault()
    try {
      const { email, password, type, accountInfo } = formData
      const selectedServiceValues = selectedServices.map(
        (selectedService) => selectedService.value,
      )
      await axios
        .post(`${process.env.REACT_APP_API_URL}/api/register`, {
          email: email,
          password: password,
          type: type,
          accountInfo: {
            name: accountInfo.name,
            description: description,
            services: selectedServiceValues,
            phoneNumber: accountInfo.phoneNumber.replace(/\D/g, ''),
            address: accountInfo.address,
            contactEmailAddress: email,
          },
        })
        .then(
          (res) => {
            alert(
              'Provider Registation Successful. Please Login to your New Account!',
            )
            navigate('/provider/login')
          },
          (fail) => {
            alert(
              'Oops...an error occurred. Please make sure all of your entered information is correct and try again.',
            )
            console.error(fail) // Error!
          },
        )
    } catch (err) {
      alert(err)
    }
  }

  return (
    <div className="bg-gradient-to-r from-[#999999] via-[#565656] to-[#565656]">
      <a href="/">
        <img className="inset-y-0 h-28" src={logo} alt="logo" />
      </a>
      <div class="flex flex-col items-center  mx-auto md:h-screen ">
        <div class="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 class="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Create a Service Provider Account
            </h1>
            <form class="space-y-4 " action="#" onSubmit={save}>
              <div className="">
                <input
                  type="text"
                  name="accountInfo.name"
                  value={formData.accountInfo.name}
                  onChange={handleChange}
                  placeholder="Business Name"
                  required
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>

              <div>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Email"
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  required=""
                />
              </div>

              <Autocomplete onLoad={onLoad} onPlaceChanged={onPlaceChanged}>
                <div>
                  <input
                    placeholder="Business Address"
                    required
                    autoComplete="new-password"
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
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                />
              </div>
              <ProviderDescription
                onDescriptionChange={handleDescriptionChange}
              />
              <Select
                options={services}
                placeholder="What services are you offering?"
                value={selectedServices}
                onChange={handleSelect}
                isSearchable={true}
                isMulti
                className="bg-custom-gray"
                required
              />
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
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
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
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
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

export default ProviderSignUp
