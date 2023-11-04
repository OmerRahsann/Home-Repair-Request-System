import React from 'react'
import ImageSlider from '../ImageSlider'
import { Fragment, useState } from 'react'
import RequestDetails from '../Customer/RequestDetails'
import ServiceRequestModal from '../Customer/ServiceRequestModal'

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
        <div className="shadow-md border-2 border-gray-400 rounded-md">
          <ImageSlider images={request.pictures} />
          <div className="bg-custom-grain p-2 flex flex-col">
            <h1 className="text-[2.5vh] font-semibold sm:text-[1vh] md:text-[2vh] lg:text-[2.5vh]">
              {request.title}
            </h1>
          </div>
        </div>
        <ServiceRequestModal
          isVisible={showModal}
          onClose={() => setShowModal(false)}
          isFinal={true}
        ></ServiceRequestModal>
      </div>
    </Fragment>
  )
}

export default ServiceRequestCard
