import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'AuthContext'
import axios from 'axios'
import { Autocomplete } from '@react-google-maps/api'
import logo from '../../Logos/mainLogo.png'
import NavBarProvider from 'components/Navbar/NavBarProvider'
import Select from 'react-select'
import Review from '../../components/ServiceProviderHome/Review'
import ServiceRequestModal from '../../components/Customer/ServiceRequestModal'

export const ProviderProfile = () => {
  const navigate = useNavigate()
  const { accessAcount } = useAuth()
  const [autoComplete, setAutoComplete] = useState(null)
  const [edit, setEdit] = useState(false)
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
    <div className="bg-custom-gray">
      <NavBarProvider />
      <div className="flex flex-col items-center  mx-auto pt-2  ">
        <section style={{ backgroundColor: '#eee', padding: '20px' }}>
          <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginTop: '20px',
              }}
            >
              {/* Left Column */}
              <div style={{ flex: '1', marginRight: '20px' }}>
                <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                  <img
                    src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-chat/ava3.webp"
                    alt="avatar"
                    style={{ width: '150px', borderRadius: '50%' }}
                  />
                  <p className="text-muted mb-1">Full Stack Developer</p>
                  <p className="text-muted mb-4">Bay Area, San Francisco, CA</p>
                  <div style={{ display: 'flex', justifyContent: 'center' }}>
                   
                    <button
                      style={{
                        backgroundColor: 'white',
                        padding: '10px',
                        borderRadius: '5px',
                        border: '1px solid #007BFF',
                      }}
                    >
                      Message
                    </button>
                  </div>
                </div>

               
              </div>
              {/* Right Column */}
              <div style={{ flex: '2' }}>
                <div style={{ marginBottom: '20px' }}>
                  <h4>Full Name</h4>
                  <p className="text-muted">Johnatan Smith</p>
                </div>
                <hr />

                <div style={{ marginBottom: '20px' }}>
                  <h4>Email</h4>
                  <p className="text-muted">example@example.com</p>
                </div>
                <hr />

                <div style={{ marginBottom: '20px' }}>
                  <h4>Phone</h4>
                  <p className="text-muted">(097) 234-5678</p>
                </div>
                <hr />

                <div style={{ marginBottom: '20px' }}>
                  <h4>Mobile</h4>
                  <p className="text-muted">(098) 765-4321</p>
                </div>
                <hr />

                <div>
                  <h4>Address</h4>
                  <p className="text-muted">Bay Area, San Francisco, CA</p>
                </div>
              </div>
            </div>
          </div>
        </section>
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 p">
          <ServiceRequestModal isVisible={edit} onClose={() => setEdit(false)}>
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
                <Select isMulti className="bg-custom-gray" />
              </form>
            </div>
          </ServiceRequestModal>
        </div>
      </div>
      <div className="p-3">
        <div>
          <h1 className="text-center font-bold">My Reviews</h1>
          <div className=" overflow-y-scroll bg-white border-black border-2">
            <div>
              <Review />
              <Review />
              <Review />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
