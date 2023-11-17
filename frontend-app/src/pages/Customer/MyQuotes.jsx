import NavBar from 'components/Navbar/NavBar'
import React, { useState, useEffect } from 'react'
import axios from 'axios'
import { checkIsCustomerLoggedIn } from 'AuthContext'
import QuoteDetails from 'components/Quotes/QuoteDetails'
import { Rating } from '@smastrom/react-rating'
import {
  CheckCircleIcon,
  UserCircleIcon,
  XCircleIcon,
} from '@heroicons/react/24/outline'

import '@smastrom/react-rating/style.css'

export const MyQuotes = () => {
  const [serviceRequests, setServiceRequests] = useState([])
  const emailRequests = ['email1', 'email24']
  const [loggedIn, setLoggedIn] = useState(false) // Initialize loggedIn with a default value
  const [showModal, setShowModal] = useState(false)
  const iconSize = window.innerWidth > 768 ? 60 : 30

  const getServiceRequests = async () => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/customer/service_request`,
        { withCredentials: true },
      )
      setServiceRequests(response.data)
      console.log(response.data)
    } catch (error) {
      console.error('Error fetching service requests:', error)
    }
  }

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsCustomerLoggedIn()
        setLoggedIn(isLoggedIn) // Set the loggedIn state based on the result of the Promise
        if (loggedIn) {
          return (
            <h1>
              You are not logged in. Please sign in to make a request and get on
              the Radar!
            </h1>
          )
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      }
    }

    fetchData() // Call the function when the component mounts
    getServiceRequests()
    console.log(serviceRequests)

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, [])

  return (
    <div>
      <div>
        <NavBar isLoggedIn={loggedIn} />
      </div>
      {serviceRequests.length !== 0 ? (
        <div className="p-1 flex flex-row">
          <div className="h-[90vh] grid grid-cols-1 gap-8 p-2 sm:p-4 md:p-6 lg:p-8 xl:p-10 w-2/3 border-r border-gray-300 overflow-y-auto custom-scrollbar">
            {serviceRequests.map((request) => (
              <div key={request.id}>
                {/* Use the RequestDetails component to display request details */}
                <QuoteDetails quote={request} />
              </div>
            ))}
          </div>

          <div className="w-1/3  h-[90vh] overflow-y-auto">
            <p className="text-center pb-2">
              <strong>Email Permission Requests: </strong>
            </p>
            {emailRequests.map((emailRequest, i) => (
              <div className="rounded-sm border border-gray-400 ">
                <div className="text-center font-bold">
                  <a href="#">{/*quote.provider*/}Ben's Roofing</a>
                </div>

                <div>
                  <div className="flex flex-row justify-between">
                    <UserCircleIcon width={iconSize} />
                    <div className="flex flex-col justify-center items-center">
                      <p className='text-gray-400'>certified plumbing</p>
                      <Rating value={5} style={{ maxWidth: 75 }} />
                    </div>
                    <div className="flex flex-row">
                      <XCircleIcon color="maroon" width={30} />
                      <CheckCircleIcon color="green" width={30} />
                    </div>
                  </div>
                 <p className='text-center'>{new Date().toISOString()}</p>
                </div>
                <div className="flex flex-row"></div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <h1 className="font-bold text-center text-xl">
          You do not have any quotes yet.
        </h1>
      )}
    </div>
  )
}
