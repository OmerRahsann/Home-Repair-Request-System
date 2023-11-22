import React, { useState, useEffect } from 'react'
import NavBarProvider from 'components/Navbar/NavBarProvider'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

export default function Appointments() {
  const [emailRequests, setEmailRequests] = useState([])
  const [appointments, setAppointments] = useState([])

  const navigate = useNavigate()

  function checkUnauthorized(error) {
    if (error.status == 403) {
      navigate('/provider/login')
    } else {
      throw error
    }
  }

  async function getAcceptedEmailRequests() {
    axios.get('/api/provider/email_requests')
      .then(response => setEmailRequests(response.data))
      .catch(checkUnauthorized)
      .catch(error => console.log(error));
  }
  async function getUpdatedAppointments() {
    axios.get('/api/provider/appointments/updated')
      .then(response => setAppointments(response.data))
      .catch(checkUnauthorized)
      .catch(error => console.log(error));
  }

  useEffect(() => {
    getAcceptedEmailRequests()
    getUpdatedAppointments()
  }, [])

  useEffect(() => {
    console.log(emailRequests)
    console.log(appointments)
  }, [emailRequests, appointments])

  return (
    <div>
      <NavBarProvider />
      <div className='p-2 flex flex-row'>
        <div className='w-1/2'>
          <p className='text-center pb-2'>
            <strong>Updated Appointments:</strong>
          </p>
          <div>
            {/* Just to show that its there */}
            {(appointments.map((appointment) => (
              <p style={{'overflow-wrap': 'anywhere'}}>{JSON.stringify(appointment)}</p>
            )))}
          </div>
        </div>
        <div className='w-1/2'>
          <p className='text-center pb-2'>
            <strong>Confirmed Email Requests:</strong>
          </p>
          <div>
            {/* Just to show that its there */}
            {(emailRequests.map((emailRequest) => (
              <p style={{'overflow-wrap': 'anywhere'}}>{JSON.stringify(emailRequest)}</p>
            )))}
          </div>
        </div>
      </div>
    </div>
  )
}
