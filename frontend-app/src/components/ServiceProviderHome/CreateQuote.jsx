import React, {useState} from 'react'
import ProviderDescription from './ProviderDescription'
import ProviderCalendar from './ProviderCalendar'
import PriceRangePicker from './PriceRangePicker'
import axios from 'axios'

export const CreateQuote = (request) => {
  const [appointmentModel, setAppointmentModel] = useState([]);
  const sendQuote = () => {
    const response = axios.post(`${process.env.REACT_APP_API_URL}/api/provider/service_requests/${request.id}/appointments/create`,
    appointmentModel,
    {withCredentials: true})
  }
  return (
    <div className="h-[100vh] p-4">
      <div className="font-bold text-center text-xl">Send a Quote</div>
      <p className="text-lg leading-6  p-2">
       <strong className='text-custom-maroon font-bold'> 🛠️ Get started with your quote. To send a quote, follow these steps: 🛠️</strong>
        <span className="block mt-2 pl-2">
          ✅ Select your estimated price range for the services.
        </span>
        <span className="block mt-2 pl-2">
        ✅ Choose a day that best fits your schedule.
        </span>
        <span className="block mt-2 pl-2">
        ✅ Optionally, include a personalized message to the customer with any
          extra information, such as contact details, etc.
        </span>
      </p>
      <PriceRangePicker />
      <div>
        <p className='text-custom-maroon font-bold pt-4'>
          <strong>Pick a day that fits your schedule: </strong>
        </p>
        <ProviderCalendar />
      </div>
      <p className="text-md text-custom-maroon font-bold pt-4">
        <strong>Attatch a message:</strong>
      </p>
      <ProviderDescription />
      <div className='pt-4'></div>
      <button 
        onClick={sendQuote}
        className=" w-full text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800">
        Send Quote
      </button>
      <div className='pt-4'></div>
    </div>
  )
}
