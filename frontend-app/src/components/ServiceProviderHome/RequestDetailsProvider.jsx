import React, { Fragment, useEffect, useState } from 'react'
import ImageSlider from '../ImageSlider'
import {
  createRoundedRange,
  extractTownAndStateFromAddress,
} from '../../Helpers/helpers'
import Map from '../Map/Map'
import { CreateQuote } from './CreateQuote'
import ServiceRequestModal from 'components/Customer/ServiceRequestModal'
import axios from 'axios'

function RequestDetailsProvider({ request }) {
  const [showSecondStep, setShowSecondStep] = useState(false)
  const [email, setEmail] = useState("")
  const handleClick = async () => {
    setShowSecondStep(true)
  }

  const createQuote = () => {
    return (
      <ServiceRequestModal
        isVisible={showSecondStep}
        onClose={() => setShowSecondStep(false)}
        last={true}
        isFinal={true}
      >
        <CreateQuote />
      </ServiceRequestModal>
    )
  }

  const requestCustomerEmail = async (event) => {
    event.preventDefault()
    try {
      const response = await axios.post(
        `${process.env.REACT_APP_API_URL}/api/provider/service_requests/${request.id}/email/request`,
        null
        , { withCredentials: true }
      )

      const data = response.data
      console.log(data)
    } catch (error) {
      alert('There was an error sending your email request. Please try again.')
      console.error('Erro Sending Email Request:', error)
    }
  }

  const getCustomerEmail = async () => {
    
    try {
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}/api/provider/service_requests/${request.id}/email`
        , { withCredentials: true }
      )
      setEmail(response.data.email)

      const data = response.data
      console.log(data)
    } catch (error) {
      alert('There was an error sending your email request. Please try again.')
      console.error('Erro Sending Email Request:', error)
    }
  }

  useEffect(() => {
    getCustomerEmail()

  }, []) 

  return (
    <Fragment>
      <div>
        <div
          className="shadow-md border-2 border-gray-400 rounded-md"
        >
          <ImageSlider images={request.pictures} />
          <div className="bg-custom-grain p-2 flex flex-col">
            <div className="flex flex-row justify-between">
              <h1 className="text-[2.5vh] font-bold text-custom-maroon">
                {request.title}
              </h1>

              {!email ? (
              <button
                onClick={requestCustomerEmail}
                className="text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
              >
                Request Email
              </button>) : (
                <div className='flex flex-row'>
              <button 
              onClick={handleClick}
              className='text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'>
                Send Quote
              </button>
              <break className="pl-2"></break>
              <button className='text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'>
              <a  href={`mailto:${email}`}>Send Email</a>
              </button>
              </div>
              )}
              
            </div>
            <h2>
              <strong>Description: </strong>
              {request.description}
            </h2>
            <h2>
              <strong>Creation Date: </strong>{' '}
              {new Date(request.creationDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </h2>
            <h2>
              <strong>Service Category: </strong>
              {request.service}
            </h2>
            <h2>
              <strong>Desired Price Range: </strong>$
              {createRoundedRange(request.dollars)}
            </h2>
            <h2>
              <strong>Status: </strong>
              {request.status}
            </h2>
            <h2>
              <strong>Location: </strong>
              {extractTownAndStateFromAddress(request.address)}
            </h2>
          </div>
          <div>
            <Map address={request.address} isProvider={true} />
          </div>
        </div>
        {showSecondStep && createQuote()}
      </div>
    </Fragment>
  )
}

export default RequestDetailsProvider
