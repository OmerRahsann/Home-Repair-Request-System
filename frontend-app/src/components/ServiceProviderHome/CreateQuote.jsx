import React from 'react'
import ProviderDescription from './ProviderDescription'
import ProviderCalendar from './ProviderCalendar'
import PriceRangePicker from './PriceRangePicker'

export const CreateQuote = () => {
  return (
    <div className='h-[100vh] p-4'>
      <div className='font-bold text-center text-xl'>
      Send a Quote
      </div>
      <PriceRangePicker/>
      <p className="text-lg leading-6 text-gray-700">
  Get started with your quote. To send a quote, follow these steps:

  <span className="block mt-2">
    1. Select your estimated price range for the services.
  </span>

  <span className="block mt-2">
    2. Choose a day that best fits your schedule.
  </span>

  <span className="block mt-2">
    3. Optionally, include a personalized message to the customer with any extra information, such as contact details, etc.
  </span>
</p>

<p><strong>Your Calendar:</strong></p>
      <div className=''>
        <ProviderCalendar/>
      </div>
      <p className='text-md'><strong>Attatch a message:</strong></p>
      <ProviderDescription/>
      <div>
        <p><strong>Pick a day that fits your schedule: </strong></p>
      </div>
    </div>
  )
}
