import React, { Fragment, useState } from 'react'
import ImageSlider from './ImageSlider'
import ServiceRequestModal from './Customer/ServiceRequestModal'
import ServiceRequestFinal from './Customer/ServiceRequestFinal'

function RequestDetails({ request }) {
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
            <h1 className="text-[2.5vh] font-semibold">{request.title}</h1>
            <h2 className="text-[1.5vh]">{request.address}</h2>
          </div>
        </div>
        <ServiceRequestModal
          isVisible={showModal}
          onClose={() => setShowModal(false)}
          size={100}
        >
          <ServiceRequestFinal request={request} />
        </ServiceRequestModal>
      </div>
    </Fragment>
  )
}

export default RequestDetails
