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
import CustomerCalendar from '../../components/Customer/CustomerCalendar'

export const MyQuotes = () => {
  const [serviceRequests, setServiceRequests] = useState([])
  const [emailRequests, setEmailRequests] = useState([])
  const [loggedIn, setLoggedIn] = useState(false) // Initialize loggedIn with a default value
  const [showModal, setShowModal] = useState(false)
  const [action, setAction] = useState(false)

  const getPendingEmailRequests = async () => {
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/customer/email_requests`,
        {
          withCredentials: true,
        },
      )
      setEmailRequests(response.data)
      console.log(response.data)
      // Set the data to your state or do further processing as needed
    } catch (error) {
      console.error('Error fetching pending email requests:', error)
    }
  }

  const acceptEmailRequest = async (emailRequestId) => {
    try {
      await axios.post(
        `${process.env.REACT_APP_API_URL}/api/customer/email_requests/${emailRequestId}/accept`,
        {},
        {
          withCredentials: true,
        },
      )
      setAction(true)

      // Handle success, update state, or navigate to another page as needed
    } catch (error) {
      console.error('Error accepting email request:', error)
    }
  }

  const rejectEmailRequest = async (emailRequestId) => {
    try {
      await axios.post(
        `${process.env.REACT_APP_API_URL}/api/customer/email_requests/${emailRequestId}/reject`,
        null,
        {
          withCredentials: true,
        },
      )
      setAction(true)

      // Handle success, update state, or navigate to another page as needed
    } catch (error) {
      console.error('Error rejecting email request:', error)
    }
  }

  const [iconSize, setIconSize] = useState(20)

  useEffect(() => {
    const handleResize = () => {
      // Adjust the icon size based on the screen width
      const newSize = window.innerWidth < 768 ? 25 : 40 // Adjust this condition as needed
      setIconSize(newSize)
    }

    // Set initial size
    handleResize()

    // Add event listener for window resize
    window.addEventListener('resize', handleResize)

    // Clean up the event listener when the component unmounts
    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, []) // Empty dependency array ensures the effect runs only once

  useEffect(() => {
    setAction(false)
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
    getPendingEmailRequests()
    console.log(serviceRequests)

    // Define the checkIsCustomerLoggedIn function here or import it from where it's defined
  }, [action])

  return (
    <div>
      <div>
        <NavBar isLoggedIn={loggedIn} />
      </div>

      <div className="p-1 flex flex-row">
        <div className="w-2/3">
          <CustomerCalendar />
        </div>

        <div className="p-2"></div>
        <div className="w-1/3  pl-2 border-l ">
          <p className="pb-2 text-center">
            <strong>Email Permission Requests: </strong>
          </p>
          {/* Add more content for the right side as needed */}

          {emailRequests.length !== 0 ? (
            <div className="h-screen overflow-y-auto flex flex-col">
              {emailRequests.map((emailRequest, i) => (
                <div
                  key={i}
                  className="max-w-md rounded-sm border border-gray-400 text-sm p-2 mb-4 w-full"
                >
                  <div className="md:pl-4 font-bold">
                    {emailRequest.serviceProvider.name}
                  </div>
                  <div className="flex flex-col mt-2">
                    <div className="flex flex-col md:flex-row justify-between items-start">
                      <div className="flex items-center mb-2 md:mb-0">
                        {iconSize > 25 ? (
                          <UserCircleIcon width={iconSize} />
                        ) : null}
                        <div className="flex flex-col text-[1.5vh]">
                          <p className="text-gray-700">
                            {emailRequest.serviceProvider.contactEmailAddress}
                          </p>
                          <p className="text-gray-700">
                            {emailRequest.serviceProvider.phoneNumber}
                          </p>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <XCircleIcon
                          color="maroon"
                          width={iconSize}
                          onClick={() => rejectEmailRequest(emailRequest.id)}
                        />
                        <CheckCircleIcon
                          color="green"
                          width={iconSize}
                          onClick={() => acceptEmailRequest(emailRequest.id)}
                        />
                      </div>
                    </div>
                    <p className="text-xs text-gray-500">
                      {new Date(emailRequest.requestTimestamp).toLocaleString()}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : null}
        </div>
      </div>
    </div>
  )
}
