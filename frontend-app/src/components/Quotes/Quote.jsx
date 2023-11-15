import ProviderCalendar from 'components/ServiceProviderHome/ProviderCalendar'
import React from 'react'
import ImageSlider from 'components/ImageSlider'

export const Quote = ({quote}) => {
  return (
    <div>
        <div className="p-2 ">
              <ImageSlider images={quote.pictures} />
              <div className="p-2 flex flex-col ">
                <div className="flex flex-row justify-between">
                  <h1 className="font-bold text-[3.5vh]">{quote.title}</h1>
                </div>
               
                <p>
                  <strong className="font-semibold">Description:</strong>{' '}
                  Hello, my name is X and I viewed your request and believe that you should hire me for the job...blah blah blah.
                  Please view my calendar and select one of the open slots. I provided you a quote range below which is an overall estimate of how much I will charge. 
                </p>
                <h2>
                  <strong className="font-semibold">Quote: </strong> $
                  500-1000
                </h2>
              </div>
            </div>
            <div className='p-2 border-zinc-500 border-4 '>
            <ProviderCalendar customerView={true}/>

            </div>
        
        <div className='flex flex-col jus pt-2 p-2 items-center'>
                <button className='text-white w-[20vw] bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'>Accept</button>
                <div className='p-2'></div>
                <button className='text-white w-[20vw] bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'>Reject</button>
        </div>
    </div>
  )
}
