import React, { Fragment, useState } from 'react'
import ImageSlider from '../ImageSlider'
import ServiceRequestModal from 'components/Customer/ServiceRequestModal'
import { Quote } from './Quote'

function QuoteDetails({ quote }) {
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
          <ImageSlider images={quote.pictures} />
          <div className="bg-custom-grain p-2 flex flex-col">
            <h1 className="text-[2.5vh] font-semibold">{quote.title}</h1>
            <h2 className="text-[1.5vh]">{quote.address}</h2>
          </div>
        </div>
        <ServiceRequestModal
          isVisible={showModal}
          onClose={() => setShowModal(false)}
          isFinal={true}
        >
          <Quote quote={quote} />
        </ServiceRequestModal>
      </div>
    </Fragment>
  )
}

export default QuoteDetails
