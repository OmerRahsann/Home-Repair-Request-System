import React from 'react'
import ImageSlider from '../ImageSlider'
import { Fragment, useState } from 'react'
import RequestDetails from '../Customer/RequestDetails'
import ServiceRequestModal from '../Customer/ServiceRequestModal'
import ServiceRequestFinal from '../Customer/ServiceRequestFinal'
import RequestDetailsProvider from './RequestDetailsProvider'
import { createRoundedRange } from '../../Helpers/helpers'

function ServiceRequestCard({ request, selected, refProp }) {
  if (selected)
    refProp?.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  const [showModal, setShowModal] = useState(false)
  const handleClick = async () => {
    setShowModal(true)
  }

  return (
    <Fragment>
      <div>
        <div
          className="shadow-md border-2 border-gray-400 rounded-md"
          onClick={handleClick}
        >
          <ImageSlider images={request.pictures} />
          <div className="bg-custom-grain p-2 flex flex-col">
            <div className="flex flex-row justify-between">
              <h1 className="text-[2.5vh] font-semibold sm:text-[1vh] md:text-[2vh] lg:text-[2.5vh]">
                {request.title}
              </h1>
              <button
                className={`text-white bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-[1vh] rounded-lg text-sm px-3 py-1 text-center dark:bg-primary-600 dark:hover-bg-primary-700 dark:focus-ring-primary-800 hidden sm:inline-block sm:text-sm sm:px-4 sm:py-2`}
              >
                Open Job
              </button>
            </div>
            <div className="flex flex-row justify-between pr-1">
              <h1>{request.service}</h1>
              <h1 className=" text-custom-maroon">
                ${createRoundedRange(request.dollars)}
              </h1>
            </div>
          </div>
        </div>
        <ServiceRequestModal
          isVisible={showModal}
          onClose={() => setShowModal(false)}
          isFinal={true}
        >
          <RequestDetailsProvider request={request} />
        </ServiceRequestModal>
      </div>
    </Fragment>
  )
}

export default ServiceRequestCard
