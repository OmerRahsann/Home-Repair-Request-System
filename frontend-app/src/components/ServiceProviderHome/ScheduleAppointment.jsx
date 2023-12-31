import React, { useEffect, useState } from 'react'
import ProviderCalendar from './ProviderCalendar'
import axios from 'axios'

export const CreateQuote = (request) => {
  const [appointmentModel, setAppointmentModel] = useState([])
  const [selectedDate, setSelectedDate] = useState(null)
  const [notification, setNotification] = useState('')
  const [submitted, setSubmitted] = useState(false)
  useEffect(() => {
    // This effect will run whenever selectedDate changes

    // Update appointmentModel with start and end if selectedDate is defined
    if (selectedDate) {
      setAppointmentModel((prevModel) => ({
        ...prevModel,
        startTime: selectedDate.start,
        endTime: selectedDate.end,
      }))
    }
  }, [selectedDate])

  const handleDateChange = (date) => {
    console.log('Selected date:', date)
    setSelectedDate(date)
  }

  const handleDescriptionChange = (event) => {
    const text = event.target.value
    setAppointmentModel({
      ...appointmentModel,
      message: text,
    })
  }

  const sendQuote = () => {
    console.log(appointmentModel)

    axios
      .post(
        `${process.env.REACT_APP_API_URL}/api/provider/service_requests/${request.request.id}/appointments/create`,
        appointmentModel,
        { withCredentials: true },
      )
      .then((response) => {
        setNotification(
          'Your Appointment was successfully created and sent to the customer. Please view the MySchedule page to see your updated calendar.',
        )
        setSubmitted(true)
      })
      .catch((error) => {
        if (
          error.response &&
          error.response.data &&
          error.response.data.type === 'conflicting_appointment'
        ) {
          const conflictingAppointments =
            error.response.data.conflictingAppointments
          // Handle conflicting appointment error here
          window.alert('Conflicting Appointment Error:')
          console.log(conflictingAppointments)
        } else {
          window.alert(
            'There was an error sending this appointment. Please try again.',
          )
        }
      })
  }

  return (
    <div>
      {submitted ? (
        <div className="">
          <div className="text-green-600 font-semibold text-center p-6">
            {notification}
          </div>
        </div>
      ) : (
        <div className="p-4">
          <div className="font-bold text-center text-xl">
            Create an Appointment
          </div>
          <p className="text-lg leading-6  p-2 ">
            <strong className="text-custom-maroon font-extrabold">
              {' '}
              🛠️ Get started with your Appointment. To create and send an
              appointment, follow these steps: 🛠️
            </strong>
            <span className="block mt-2 ">
              ✅ Choose a day that best fits your schedule.
            </span>
            <span className="block mt-2 ">
              ✅ Optionally, include a personalized message to the customer with
              any extra information, such as contact details, etc.
            </span>
          </p>
          {/* <PriceRangePicker /> */}
          <div>
            <p className="text-custom-maroon font-bold pt-4">
              <strong>Pick a day that fits your schedule: </strong>
            </p>
            <ProviderCalendar
              request={request}
              setDate={handleDateChange}
              isQuote={true}
              createEventPopup={true}
            />
          </div>
          <p className="text-md text-custom-maroon font-bold pt-4">
            <strong>Attatch a message:</strong>
          </p>
          <textarea
            value={appointmentModel.description}
            onChange={handleDescriptionChange}
            placeholder="Example: Hello, I saw your request for gutter cleanup services and wanted to let you know that 'My Company' has extensive experience in providing top-notch gutter cleaning solutions.

                We take pride in our attention to detail and commitment to customer satisfaction. I'm confident that our team can efficiently clear out the accumulated leaves, debris, and dirt from your gutters, ensuring they function properly and prevent any potential damage.
                
                If you have any questions or would like to discuss your specific needs further, feel free to reach out to me directly at [Your Phone Number]. I'm here to help!
                
                Looking forward to the opportunity to assist you.
                
                Best regards,
                
                My Company"
            className=" border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5  dark:border-gray-600 dark:placeholder-gray-400 "
            maxLength={500}
            rows={8}
            spellCheck
            style={{ resize: 'none' }}
            required
          />
          <div className="pt-4"></div>
          <button
            onClick={sendQuote}
            className=" w-full text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Schedule Appointment
          </button>
          <div className="pt-4"></div>
        </div>
      )}
    </div>
  )
}
