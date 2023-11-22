import React, { useState, useEffect } from 'react'
import NavBarProvider from 'components/Navbar/NavBarProvider'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

function onSameDay(date1, date2) {
  return date1.getDate() == date2.getDate() &&
    date1.getMonth() == date2.getMonth() &&
    date1.getYear() == date2.getYear();
}

function appointmentPeriod(appointment) {
  let sameDay = onSameDay(appointment.startTime, appointment.endTime)
  let start = appointment.startTime.toLocaleString()
  let end = sameDay ? appointment.endTime.toLocaleTimeString() : appointment.endTime.toLocaleString()
  return (
    <span> {start} - {end} </span>
  )
}

export default function Appointments() {
  const [emailRequests, setEmailRequests] = useState([])
  const [appointments, setAppointments] = useState([])

  const navigate = useNavigate()

  function checkUnauthorized(error) {
    if (error.response.status == 403) {
      navigate('/provider/login')
    } else {
      throw error
    }
  }

  async function getAcceptedEmailRequests() {
    axios.get('/api/provider/email_requests')
      .then(response => response.data.map(emailRequest => {
        emailRequest.updateTimestamp = new Date(emailRequest.updateTimestamp);
        return emailRequest;
      }))
      .then(responseData => setEmailRequests(responseData))
      .catch(checkUnauthorized)
      .catch(error => console.log(error));
  }
  async function getUpdatedAppointments() {
    axios.get('/api/provider/appointments/updated')
      .then(response => response.data.map(appointment => {
        appointment.creationTimestamp = new Date(appointment.creationTimestamp);
        appointment.updateTimestamp = new Date(appointment.updateTimestamp);
        appointment.startTime = new Date(appointment.startTime);
        appointment.endTime = new Date(appointment.endTime);
        return appointment;
      }))
      .then(responseData => setAppointments(responseData))
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
              <div className='mx-2 mb-2 p-1 border rounded-md shadow-md text-[2vh]'>
                <div className='flex flex-row justify-between'>
                  <div>
                    <strong>Customer: {appointment.customerInfoModel.firstName} {appointment.customerInfoModel.lastName}</strong>
                  </div>
                  <span className='text-right'>{appointmentPeriod(appointment)}</span>
                </div>
                <p className='text-right'>{appointment.status}</p>
                <p>{appointment.message}</p>
                <p className='text-right text-[1vh]'>{appointment.updateTimestamp.toLocaleString()}</p>
              </div>
            )))}
          </div>
        </div>
        <div className='w-1/2'>
          <p className='text-center pb-2'>
            <strong>Confirmed Email Requests:</strong>
          </p>
          <div className='flex-col'>
            {(emailRequests.map((emailRequest) => (
              <div className='mx-2 mb-2 p-1 border rounded-md shadow-md text-[2vh]'>
                <div className='flex flex-row justify-between'>
                  <div>
                    <strong>{emailRequest.customer.firstName} {emailRequest.customer.lastName}</strong>
                  </div>
                  <div className='text-right'>
                    <span className='px-[1em]'>📞 {emailRequest.customer.phoneNumber}</span>
                    <span>📧 <a href={"mailto:" + emailRequest.email}>{emailRequest.email}</a></span>
                  </div>
                </div>
                <p className='text-[1.75vh]'>{emailRequest.serviceRequest.title}</p>
                <p className='text-gray-700 text-[1.5vh]'>{emailRequest.serviceRequest.description}</p>
                <p className='text-right text-[1vh]'>{emailRequest.updateTimestamp.toLocaleString()}</p>
              </div>
            )))}
          </div>
        </div>
      </div>
    </div>
  )
}
